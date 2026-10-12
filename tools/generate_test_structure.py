#!/usr/bin/env python3
"""Generates data/stonegolem/structure/arena.nbt: the 9x6x9 game-test arena (stone floor, air above).

Usage: python3 tools/generate_test_structure.py   (standard library only)
"""
import gzip
import struct
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "common/src/main/resources/data/stonegolem/structure/arena.nbt"
DATA_VERSION = 5023  # Minecraft 26.3; the game upgrades older structures through DataFixerUpper anyway.
SIZE = (9, 6, 9)

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def name(n):
    data = n.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def payload(tag, value):
    if tag == TAG_INT:
        return struct.pack(">i", value)
    if tag == TAG_STRING:
        return name(value)
    if tag == TAG_LIST:
        element_tag, items = value
        return struct.pack(">bi", element_tag if items else TAG_END, len(items)) + b"".join(payload(element_tag, i) for i in items)
    if tag == TAG_COMPOUND:
        return b"".join(struct.pack(">b", t) + name(k) + payload(t, v) for k, (t, v) in value.items()) + b"\x00"
    raise ValueError(tag)


def main():
    # 26.3 palette entries use "id" (formerly "Name"); unknown keys silently become air.
    palette = [{"id": (TAG_STRING, "minecraft:stone")}, {"id": (TAG_STRING, "minecraft:air")}]
    blocks = []
    for x in range(SIZE[0]):
        for y in range(SIZE[1]):
            for z in range(SIZE[2]):
                blocks.append({
                    "pos": (TAG_LIST, (TAG_INT, [x, y, z])),
                    "state": (TAG_INT, 0 if y == 0 else 1),
                })
    root = {
        "DataVersion": (TAG_INT, DATA_VERSION),
        "size": (TAG_LIST, (TAG_INT, list(SIZE))),
        "palette": (TAG_LIST, (TAG_COMPOUND, palette)),
        "blocks": (TAG_LIST, (TAG_COMPOUND, blocks)),
        "entities": (TAG_LIST, (TAG_COMPOUND, [])),
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    # mtime=0 keeps the file byte-identical between runs.
    with gzip.GzipFile(OUT, "wb", mtime=0) as f:
        f.write(struct.pack(">b", TAG_COMPOUND) + name("") + payload(TAG_COMPOUND, root))
    print("Wrote", OUT)


if __name__ == "__main__":
    main()
