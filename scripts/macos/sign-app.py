#!/usr/bin/env python3
"""Ad-hoc sign a macOS .app with rcodesign and check the result.

Signs every regular-file Mach-O (deepest first), then the bundle last.
Hardened runtime stays off: Java's JIT would need entitlements for it, and
these builds are not notarised. Passing --code-signature-flags linker-signed
replaces any flags already on the binary (including the JRE's RUNTIME bit).
rcodesign then drops linker-signed, because it is not a linker, and the
signature that remains is ad-hoc.

rcodesign 0.29.0 verify only accepts a Mach-O file, and on an ad-hoc signature
it reports one known error: the CMS blob is empty. Page hashes are still
checked. Any other verify problem fails the run.
"""
import hashlib
import os
import plistlib
import stat
import struct
import subprocess
import sys

CPU = {"arm64": 0x0100000C, "x86_64": 0x01000007}
VERIFY_OK = {
    "(the verify command is known to be buggy and gives misleading results; we highly recommend using Apple's tooling until this message is removed)",
    "CMS error: missing further values (at position 0)",
    "Error: problems reported during verification",
    "no problems detected!",
    "(we do not verify everything so please do not assume that the signature meets Apple standards)",
}


def rcodesign():
    env = os.environ.get("RCODESIGN")
    if env:
        return env
    return "rcodesign"


def run(args, check=True):
    proc = subprocess.run(
        [rcodesign()] + args,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )
    if check and proc.returncode != 0:
        sys.stderr.write(proc.stdout)
        raise SystemExit("rcodesign %s failed (%s)" % (" ".join(args[:2]), proc.returncode))
    return proc.returncode, proc.stdout


def cputypes(path):
    with open(path, "rb") as fh:
        magic = fh.read(4)
        if magic in (b"\xcf\xfa\xed\xfe", b"\xce\xfa\xed\xfe"):
            return [struct.unpack("<I", fh.read(4))[0]]
        if magic in (b"\xfe\xed\xfa\xcf", b"\xfe\xed\xfa\xce"):
            return [struct.unpack(">I", fh.read(4))[0]]
        if magic in (b"\xca\xfe\xba\xbe", b"\xbe\xba\xfe\xca"):
            endian = ">" if magic == b"\xca\xfe\xba\xbe" else "<"
            count = struct.unpack(endian + "I", fh.read(4))[0]
            # A Java class file shares the big-endian cafebabe magic and has
            # a minor version of 0 here. Fat headers have a small arch count.
            if count < 1 or count > 8:
                return None
            found = []
            for _ in range(count):
                header = fh.read(20)
                if len(header) < 20:
                    return None
                found.append(struct.unpack(endian + "I", header[:4])[0])
            return found
    return None


def machos(app):
    found = []
    for dirpath, dirnames, filenames in os.walk(app):
        dirnames.sort()
        for name in sorted(filenames):
            path = os.path.join(dirpath, name)
            if os.path.islink(path) or not os.path.isfile(path):
                continue
            types = cputypes(path)
            if not types:
                continue
            rel = os.path.relpath(path, app)
            found.append((rel, types))
    found.sort(key=lambda item: (-item[0].count("/"), item[0]))
    return found


def check_arch(app, arch):
    expect = CPU[arch]
    rows = machos(app)
    if not rows:
        raise SystemExit("no Mach-O files in %s" % app)
    bad = []
    for rel, types in rows:
        if types != [expect]:
            bad.append("%s cpu=%s" % (rel, ",".join(hex(t) for t in types)))
    if bad:
        raise SystemExit("Mach-O architecture is not %s:\n%s" % (arch, "\n".join(bad)))
    natives = os.path.join(app, "Contents", "Resources", "natives", "osx-arm64")
    if arch == "arm64":
        for name in ("liblwjgl.dylib", "openal.dylib"):
            if not os.path.isfile(os.path.join(natives, name)):
                raise SystemExit("missing arm64 LWJGL native %s" % name)
    elif os.path.isdir(natives):
        raise SystemExit("x86_64 app contains osx-arm64 natives")
    java = os.path.join(app, "Contents", "Resources", "jre", "bin", "java")
    if not os.path.isfile(java):
        raise SystemExit("missing bundled java")
    return rows


def sign(app, arch):
    rows = check_arch(app, arch)
    print("signing %d Mach-O files as %s, then the bundle" % (len(rows), arch))
    for rel, _types in rows:
        path = os.path.join(app, rel)
        # linker-signed clears preserved flags (the JRE's RUNTIME bit). rcodesign
        # removes that bit and leaves an ad-hoc signature, with no hardened runtime.
        run(["sign", "--code-signature-flags", "linker-signed", path])
    run(["sign", "--code-signature-flags", "main:linker-signed", app])
    verify(app, arch)


def verify_macho(path):
    rc, out = run(["verify", path], check=False)
    lines = [line.strip() for line in out.splitlines() if line.strip()]
    extra = [line for line in lines if line not in VERIFY_OK]
    if rc == 0 and not extra:
        return
    if extra or "CMS error: missing further values (at position 0)" not in lines:
        sys.stderr.write(out)
        raise SystemExit("rcodesign verify failed: %s" % path)


def signature_flags(app):
    _rc, out = run(["print-signature-info", app])
    current = None
    flags = {}
    for line in out.splitlines():
        if line.startswith("- path: "):
            current = line.split(": ", 1)[1].strip()
        elif current and current not in flags and "flags: CodeSignatureFlags(" in line:
            flags[current] = line.split("flags: ", 1)[1].strip()
    return flags


def check_seal(app):
    seal_path = os.path.join(app, "Contents", "_CodeSignature", "CodeResources")
    if not os.path.isfile(seal_path):
        raise SystemExit("bundle has no CodeResources seal")
    with open(seal_path, "rb") as fh:
        seal = plistlib.load(fh)
    files2 = seal.get("files2") or {}
    if not files2:
        raise SystemExit("CodeResources has no files2 entries")
    for rel, info in files2.items():
        path = os.path.join(app, "Contents", rel)
        if not isinstance(info, dict):
            raise SystemExit("unexpected CodeResources entry for %s" % rel)
        if "symlink" in info:
            if not os.path.islink(path) or os.readlink(path) != info["symlink"]:
                raise SystemExit("CodeResources symlink mismatch: %s" % rel)
            continue
        digest = info.get("hash2")
        if not isinstance(digest, (bytes, bytearray)) or len(digest) != 32:
            raise SystemExit("unexpected CodeResources hash for %s" % rel)
        if not os.path.isfile(path) or os.path.islink(path):
            raise SystemExit("sealed file missing: %s" % rel)
        actual = hashlib.sha256(open(path, "rb").read()).digest()
        if actual != digest:
            raise SystemExit("CodeResources hash mismatch: %s" % rel)


def verify(app, arch):
    rows = check_arch(app, arch)
    rc, out = run(["verify", app], check=False)
    if rc == 0:
        pass
    elif "only works on Mach-O binaries" not in out:
        sys.stderr.write(out)
        raise SystemExit("rcodesign verify on the bundle failed unexpectedly")
    else:
        print("rcodesign 0.29.0 verify does not accept a bundle; checking every Mach-O and the CodeResources seal")
    flags = signature_flags(app)
    for rel, _types in rows:
        got = flags.get(rel)
        if got != "CodeSignatureFlags(ADHOC)":
            raise SystemExit("%s signature flags are %s, want ADHOC only" % (rel, got))
        verify_macho(os.path.join(app, rel))
    ident = flags.get("Contents/MacOS/confu")
    if ident is None:
        raise SystemExit("main executable has no signature")
    check_seal(app)
    print("verified %d Mach-O files, ad-hoc, no hardened runtime" % len(rows))


def manifest(app):
    rows = []
    for dirpath, dirnames, filenames in os.walk(app):
        dirnames.sort()
        rel_dir = os.path.relpath(dirpath, app)
        if rel_dir == ".":
            rel_dir = ""
        mode = stat.S_IMODE(os.lstat(dirpath).st_mode)
        rows.append("dir %04o %s" % (mode, rel_dir))
        for name in sorted(filenames):
            path = os.path.join(dirpath, name)
            rel = os.path.relpath(path, app)
            st = os.lstat(path)
            mode = stat.S_IMODE(st.st_mode)
            if stat.S_ISLNK(st.st_mode):
                rows.append("link %04o %s -> %s" % (mode, rel, os.readlink(path)))
            elif stat.S_ISREG(st.st_mode):
                digest = hashlib.sha256(open(path, "rb").read()).hexdigest()
                rows.append("file %04o %s %s" % (mode, digest, rel))
            else:
                rows.append("other %04o %s" % (mode, rel))
    return "\n".join(rows) + "\n"


def main(argv):
    if len(argv) < 2:
        raise SystemExit("usage: sign-app.py sign|verify APP arm64|x86_64 | manifest APP | compare A B")
    cmd = argv[1]
    if cmd in ("sign", "verify"):
        app, arch = argv[2], argv[3]
        if arch not in CPU:
            raise SystemExit("arch must be arm64 or x86_64")
        if cmd == "sign":
            sign(app, arch)
        else:
            verify(app, arch)
    elif cmd == "manifest":
        sys.stdout.write(manifest(argv[2]))
    elif cmd == "compare":
        a = manifest(argv[2])
        b = manifest(argv[3])
        if a != b:
            raise SystemExit("bundle changed after signing (content, symlink target, or mode)")
        print("bundle matches the signed tree")
    else:
        raise SystemExit("unknown command %s" % cmd)


if __name__ == "__main__":
    main(sys.argv)
