#!/usr/bin/env python3
"""Ad-hoc sign the Mach-O files of a macOS .app with rcodesign, then check it.

Signs every regular-file Mach-O (deepest first). Hardened runtime stays off:
Java's JIT would need entitlements for it, and these builds are not notarised.
Passing --code-signature-flags linker-signed replaces any flags already on the
binary (including the JRE's RUNTIME bit). rcodesign then drops linker-signed,
because it is not a linker, and the signature that remains is ad-hoc.

The bundle itself is not sealed. Contents/MacOS/confu is a /bin/sh script, and
rcodesign 0.29.0 only signs bundles whose main executable is a Mach-O. So the
bundle has no _CodeSignature, and the checks below cover the plain files
(Info.plist, PkgInfo, launcher) directly.

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
import tempfile

CPU = {"arm64": 0x0100000C, "x86_64": 0x01000007}
# Lowest macOS each architecture may need: the x64 JRE is built for 11, and the
# arm64 LWJGL openal.dylib native is built for 12.
MIN_MACOS = {"arm64": "12.0", "x86_64": "11.0"}
VERIFY_OK = {
    "(the verify command is known to be buggy and gives misleading results; we highly recommend using Apple's tooling until this message is removed)",
    "CMS error: missing further values (at position 0)",
    "Error: problems reported during verification",
    "no problems detected!",
    "(we do not verify everything so please do not assume that the signature meets Apple standards)",
}
SYSTEM_PREFIXES = ("/usr/lib/", "/System/Library/")
# Replaces the entitlements the Zulu and Temurin binaries came with. Without the
# hardened runtime they do nothing, but the bundle should not carry them.
EMPTY_ENTITLEMENTS = (
    b'<?xml version="1.0" encoding="UTF-8"?>\n'
    b'<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">\n'
    b'<plist version="1.0"><dict/></plist>\n'
)
LC_LOAD_DYLIB = 0x0C
LC_RPATH = 0x8000001C
LC_LOAD_WEAK_DYLIB = 0x80000018
LC_REEXPORT_DYLIB = 0x8000001F
LC_LOAD_UPWARD_DYLIB = 0x80000023
LC_VERSION_MIN_MACOSX = 0x24
LC_ID_DYLIB = 0x0D
LC_BUILD_VERSION = 0x32
DEP_CMDS = (LC_LOAD_DYLIB, LC_LOAD_WEAK_DYLIB, LC_REEXPORT_DYLIB, LC_LOAD_UPWARD_DYLIB)


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
    print("signing %d Mach-O files as %s; the bundle stays unsealed" % (len(rows), arch))
    with tempfile.TemporaryDirectory() as tmp:
        ent = os.path.join(tmp, "empty.plist")
        with open(ent, "wb") as fh:
            fh.write(EMPTY_ENTITLEMENTS)
        for rel, _types in rows:
            path = os.path.join(app, rel)
            run(["sign", "--code-signature-flags", "linker-signed", "--entitlements-xml-file", ent, path])
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


def macho_flags(path):
    _rc, out = run(["print-signature-info", path])
    for line in out.splitlines():
        if "flags: CodeSignatureFlags(" in line:
            return line.split("flags: ", 1)[1].strip()
    return None


def check_no_seal(app):
    for dirpath, dirnames, _files in os.walk(app):
        if "_CodeSignature" in dirnames:
            raise SystemExit("unexpected bundle seal in %s" % os.path.relpath(dirpath, app))


def check_launcher(app):
    path = os.path.join(app, "Contents", "MacOS", "confu")
    if os.path.islink(path) or not os.path.isfile(path):
        raise SystemExit("Contents/MacOS/confu is missing or not a regular file")
    if not os.access(path, os.X_OK):
        raise SystemExit("Contents/MacOS/confu is not executable")
    with open(path, "rb") as fh:
        if fh.read(10) != b"#!/bin/sh\n":
            raise SystemExit("Contents/MacOS/confu does not start with #!/bin/sh")


def check_bundle_files(app, arch):
    with open(os.path.join(app, "Contents", "Info.plist"), "rb") as fh:
        info = plistlib.load(fh)
    want = {
        "CFBundleExecutable": "confu",
        "CFBundlePackageType": "APPL",
        "CFBundleIdentifier": "com.confu.client",
        "CFBundleIconFile": "confusion3h",
        "LSMinimumSystemVersion": MIN_MACOS[arch],
        "NSHighResolutionCapable": True,
    }
    for key, value in want.items():
        if info.get(key) != value:
            raise SystemExit("Info.plist %s is %r, want %r" % (key, info.get(key), value))
    if not isinstance(info.get("NSAppleEventsUsageDescription"), str) or not info["NSAppleEventsUsageDescription"]:
        raise SystemExit("Info.plist has no NSAppleEventsUsageDescription")
    with open(os.path.join(app, "Contents", "PkgInfo"), "rb") as fh:
        if fh.read() != b"APPL????":
            raise SystemExit("Contents/PkgInfo is not APPL????")


def check_bundle_links(app):
    java_link = os.path.join(app, "Contents", "MacOS", "confu-java")
    if not os.path.islink(java_link) or os.readlink(java_link) != "../Resources/jre/bin/java":
        raise SystemExit("Contents/MacOS/confu-java must link to ../Resources/jre/bin/java")
    if not os.path.isfile(java_link):
        raise SystemExit("Contents/MacOS/confu-java does not resolve to a file")
    lib_link = os.path.join(app, "Contents", "lib")
    if not os.path.islink(lib_link) or os.readlink(lib_link) != "Resources/jre/lib":
        raise SystemExit("Contents/lib must link to Resources/jre/lib")
    if not os.path.isfile(os.path.join(lib_link, "jli", "libjli.dylib")):
        raise SystemExit("Contents/lib/jli/libjli.dylib does not resolve")


def check_icns(app):
    path = os.path.join(app, "Contents", "Resources", "confusion3h.icns")
    if os.path.islink(path) or not os.path.isfile(path):
        raise SystemExit("Contents/Resources/confusion3h.icns is missing")
    with open(path, "rb") as fh:
        data = fh.read()
    if data[:4] != b"icns" or struct.unpack(">I", data[4:8])[0] != len(data):
        raise SystemExit("confusion3h.icns has a bad header")
    seen = set()
    off = 8
    while off < len(data):
        kind = data[off:off + 4]
        size = struct.unpack(">I", data[off + 4:off + 8])[0]
        if size < 8 or off + size > len(data):
            raise SystemExit("confusion3h.icns has a bad chunk at %d" % off)
        seen.add(kind)
        off += size
    for need in (b"icp4", b"ic07", b"ic08", b"ic09", b"ic10"):
        if need not in seen:
            raise SystemExit("confusion3h.icns is missing %s" % need.decode())


def macho_load(path):
    with open(path, "rb") as fh:
        data = fh.read()
    if data[:4] != b"\xcf\xfa\xed\xfe":
        raise SystemExit("not a thin 64-bit little-endian Mach-O: %s" % path)
    ncmds = struct.unpack_from("<I", data, 16)[0]
    off = 32
    minos = None
    deps = []
    for _ in range(ncmds):
        cmd, size = struct.unpack_from("<II", data, off)
        if cmd == LC_BUILD_VERSION:
            minos = struct.unpack_from("<I", data, off + 12)[0]
        elif cmd == LC_VERSION_MIN_MACOSX:
            minos = struct.unpack_from("<I", data, off + 8)[0]
        elif cmd in DEP_CMDS or cmd in (LC_RPATH, LC_ID_DYLIB):
            name_off = struct.unpack_from("<I", data, off + 8)[0]
            end = data.index(b"\0", off + name_off)
            name = data[off + name_off:end].decode()
            prefix = {LC_RPATH: "rpath:", LC_ID_DYLIB: "id:"}.get(cmd, "")
            deps.append(prefix + name)
        off += size
    return minos, deps


def check_macho_deps(app, min_version):
    minimum = tuple(int(part) for part in min_version.split(".")[:2])
    exec_dir = os.path.join(app, "Contents", "MacOS")
    loaded = {}
    for rel, _types in machos(app):
        minos, deps = macho_load(os.path.join(app, rel))
        for dep in deps:
            if dep.startswith("id:"):
                loaded.setdefault(os.path.basename(dep[len("id:"):]), rel)
    for rel, _types in machos(app):
        path = os.path.join(app, rel)
        minos, deps = macho_load(path)
        if minos is None:
            raise SystemExit("%s has no LC_BUILD_VERSION or LC_VERSION_MIN_MACOSX" % rel)
        found = ((minos >> 16) & 0xFFFF, (minos >> 8) & 0xFF)
        if found > minimum:
            raise SystemExit("%s needs macOS %d.%d, bundle says %s" % (rel, found[0], found[1], min_version))
        rpaths = [d[len("rpath:"):] for d in deps if d.startswith("rpath:")]
        for dep in deps:
            if dep.startswith(("rpath:", "id:")) or dep.startswith(SYSTEM_PREFIXES):
                continue
            if dep.startswith("/"):
                raise SystemExit("%s links an absolute path outside the system: %s" % (rel, dep))
            if dep.startswith("@loader_path/"):
                candidates = [os.path.join(os.path.dirname(path), dep[len("@loader_path/"):])]
            elif dep.startswith("@executable_path/"):
                candidates = [os.path.join(exec_dir, dep[len("@executable_path/"):])]
            elif dep.startswith("@rpath/"):
                if os.path.basename(dep) in loaded:
                    continue
                candidates = []
                for rp in rpaths:
                    if rp.startswith("@loader_path"):
                        rp = os.path.dirname(path) + rp[len("@loader_path"):]
                    elif rp.startswith("@executable_path"):
                        rp = exec_dir + rp[len("@executable_path"):]
                    candidates.append(os.path.join(rp, dep[len("@rpath/"):]))
            else:
                continue
            if not any(os.path.isfile(os.path.normpath(c)) for c in candidates):
                raise SystemExit("%s: dependency not found in bundle: %s" % (rel, dep))


def verify(app, arch):
    rows = check_arch(app, arch)
    for rel, _types in rows:
        path = os.path.join(app, rel)
        got = macho_flags(path)
        if got != "CodeSignatureFlags(ADHOC)":
            raise SystemExit("%s signature flags are %s, want ADHOC only" % (rel, got))
        _rc, info = run(["print-signature-info", path])
        if "com.apple.security." in info:
            raise SystemExit("%s still carries entitlements" % rel)
        verify_macho(path)
    check_no_seal(app)
    check_launcher(app)
    check_bundle_files(app, arch)
    check_bundle_links(app)
    check_icns(app)
    check_macho_deps(app, MIN_MACOS[arch])
    print("verified %d Mach-O files, ad-hoc, no hardened runtime; bundle unsealed" % len(rows))


def manifest(app):
    rows = []
    for dirpath, dirnames, filenames in os.walk(app):
        dirnames.sort()
        rel_dir = os.path.relpath(dirpath, app)
        if rel_dir == ".":
            rel_dir = ""
        mode = stat.S_IMODE(os.lstat(dirpath).st_mode)
        rows.append("dir %04o %s" % (mode, rel_dir))
        linked_dirs = [d for d in dirnames if os.path.islink(os.path.join(dirpath, d))]
        for name in sorted(filenames + linked_dirs):
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
