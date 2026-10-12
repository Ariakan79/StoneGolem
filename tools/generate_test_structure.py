#!/usr/bin/env python3
"""Generates the game-test arenas (stone floor, air above):
data/stonegolem/structure/arena.nbt (9x6x9) and arena_large.nbt (17x12x17, for launch tests).

Usage: python3 tools/generate_test_structure.py   (standard library only)
"""
import gzip
import struct
from pathlib import Path

OUT_DIR = Path(__file__).resolve().parent.parent / "common/src/main/resources/data/stonegolem/structure"
DATA_VERSION = 5023  # Minecraft 26.3; the game upgrades older structures through DataFixerUpper anyway.
ARENAS = {"arena": (9, 6, 9), "arena_large": (17, 12, 17)}

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def nbt_name(n):
    data = n.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def payload(tag, value):
    if tag == TAG_INT:
        return struct.pack(">i", value)
    if tag == TAG_STRING:
        return nbt_name(value)
    if tag == TAG_LIST:
        element_tag, items = value
        return struct.pack(">bi", element_tag if items else TAG_END, len(items)) + b"".join(payload(element_tag, i) for i in items)
    if tag == TAG_COMPOUND:
        return b"".join(struct.pack(">b", t) + nbt_name(k) + payload(t, v) for k, (t, v) in value.items()) + b"\x00"
    raise ValueError(tag)


def write_arena(name, size):
    # 26.3 palette entries use "id" (formerly "Name"); unknown keys silently become air.
    palette = [{"id": (TAG_STRING, "minecraft:stone")}, {"id": (TAG_STRING, "minecraft:air")}]
    blocks = []
    for x in range(size[0]):
        for y in range(size[1]):
            for z in range(size[2]):
                blocks.append({
                    "pos": (TAG_LIST, (TAG_INT, [x, y, z])),
                    "state": (TAG_INT, 0 if y == 0 else 1),
                })
    root = {
        "DataVersion": (TAG_INT, DATA_VERSION),
        "size": (TAG_LIST, (TAG_INT, list(size))),
        "palette": (TAG_LIST, (TAG_COMPOUND, palette)),
        "blocks": (TAG_LIST, (TAG_COMPOUND, blocks)),
        "entities": (TAG_LIST, (TAG_COMPOUND, [])),
    }
    out = OUT_DIR / f"{name}.nbt"
    out.parent.mkdir(parents=True, exist_ok=True)
    # mtime=0 keeps the file byte-identical between runs.
    with gzip.GzipFile(out, "wb", mtime=0) as f:
        f.write(struct.pack(">b", TAG_COMPOUND) + nbt_name("") + payload(TAG_COMPOUND, root))
    print("Wrote", out)


def main():
    for arena, size in ARENAS.items():
        write_arena(arena, size)


if __name__ == "__main__":
    main()
