#!/usr/bin/env python3
"""Pack the PNG set from branding/gen.py into one macOS .icns file.

    python3 scripts/macos/make-icns.py branding/png/mac build/confusion3h.icns

The directory must hold icon_16.png ... icon_1024.png at exactly those sizes.
Each PNG is stored under the OSType Apple uses for its pixel size. The @2x
types (ic11 to ic14) reuse the PNG for the matching 1x size times two, so
Retina displays get a sharp image and the Dock never scales a 1x one.
"""
import io
import os
import struct
import sys

from PIL import Image

TYPES = [
    ("icp4", 16), ("icp5", 32), ("icp6", 64),
    ("ic07", 128), ("ic08", 256), ("ic09", 512), ("ic10", 1024),
    ("ic11", 32), ("ic12", 64), ("ic13", 256), ("ic14", 512),
]


def png_bytes(path, size):
    img = Image.open(path)
    if img.size != (size, size):
        raise SystemExit("%s is %dx%d, want %dx%d" % (path, img.size[0], img.size[1], size, size))
    buf = io.BytesIO()
    img.convert("RGBA").save(buf, "PNG", optimize=True)
    return buf.getvalue()


def main(argv):
    if len(argv) != 3:
        raise SystemExit("usage: make-icns.py PNG_DIR OUT.icns")
    src, dest = argv[1], argv[2]
    cache = {}
    body = []
    for ostype, size in TYPES:
        if size not in cache:
            cache[size] = png_bytes(os.path.join(src, "icon_%d.png" % size), size)
        data = cache[size]
        body.append(ostype.encode("ascii") + struct.pack(">I", len(data) + 8) + data)
    payload = b"".join(body)
    tmp = dest + ".tmp"
    with open(tmp, "wb") as fh:
        fh.write(b"icns" + struct.pack(">I", len(payload) + 8) + payload)
    os.replace(tmp, dest)
    print("wrote %s (%d bytes, %d icon types)" % (dest, os.path.getsize(dest), len(TYPES)))


if __name__ == "__main__":
    main(sys.argv)
