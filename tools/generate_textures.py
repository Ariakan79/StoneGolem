#!/usr/bin/env python3
"""Generates the first-draft textures of the Stone Golem mod.

The output is deliberately simple pixel art meant to be reworked in Blockbench. The UV layout of the entity texture
matches StoneGolemModel.createBodyLayer(); if you change the model, change BOXES below as well.

Usage: python3 tools/generate_textures.py   (requires Pillow; deterministic thanks to a fixed seed)
"""
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "common/src/main/resources/assets/stonegolem/textures"
ICON = ROOT / "common/src/main/resources/stonegolem.png"

STONE = [(112, 112, 108), (121, 121, 117), (128, 127, 122), (136, 135, 129), (104, 104, 101)]
STONE_DARK = (78, 78, 76)
STONE_DEEP = (58, 58, 57)
MOSS = [(62, 99, 36), (79, 122, 44), (95, 143, 52), (70, 110, 40)]
LICHEN = [(184, 184, 90), (201, 162, 74), (168, 176, 120)]
GLOW = [(127, 224, 208), (90, 190, 180)]

# name: (u, v, width, height, depth) - must match StoneGolemModel
BOXES = {
    "body": (0, 0, 20, 13, 11),
    "moss": (0, 56, 12, 2, 6),
    "head": (0, 24, 9, 8, 8),
    "brow": (36, 56, 9, 2, 1),
    "arm": (34, 24, 6, 20, 6),
    "leg": (0, 40, 7, 9, 7),
}


def faces(u, v, w, h, d):
    """Vanilla cube UV layout: returns face name -> (x, y, width, height)."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def fill_stone(img, rng, x, y, w, h):
    for py in range(y, y + h):
        for px in range(x, x + w):
            img.putpixel((px, py), rng.choice(STONE))
    # Darker blotches give the surface some weight.
    for _ in range(max(1, w * h // 18)):
        img.putpixel((x + rng.randrange(w), y + rng.randrange(h)), STONE_DARK)


def cracks(img, rng, x, y, w, h, count):
    for _ in range(count):
        px, py = x + rng.randrange(w), y + rng.randrange(h)
        for _ in range(rng.randint(2, 5)):
            img.putpixel((px, py), STONE_DEEP)
            px = min(max(px + rng.choice((-1, 0, 1)), x), x + w - 1)
            py = min(py + 1, y + h - 1)


def moss_cover(img, rng, x, y, w, h, density):
    for py in range(y, y + h):
        for px in range(x, x + w):
            if rng.random() < density:
                img.putpixel((px, py), rng.choice(MOSS))


def moss_drips(img, rng, x, y, w, h, max_len):
    for px in range(x, x + w):
        length = rng.randint(0, max_len)
        for py in range(y, min(y + length, y + h)):
            img.putpixel((px, py), rng.choice(MOSS))


def lichen(img, rng, x, y, w, h, count):
    for _ in range(count):
        img.putpixel((x + rng.randrange(w), y + rng.randrange(h)), rng.choice(LICHEN))


def entity_texture():
    rng = random.Random(1337)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for name, (u, v, w, h, d) in BOXES.items():
        for face, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            fill_stone(img, rng, fx, fy, fw, fh)
            if face == "top":
                moss_cover(img, rng, fx, fy, fw, fh, 0.9 if name in ("moss", "body", "head", "brow") else 0.6)
            elif face == "bottom":
                for py in range(fy, fy + fh):
                    for px in range(fx, fx + fw):
                        img.putpixel((px, py), STONE_DARK if rng.random() < 0.6 else STONE_DEEP)
            else:
                if name in ("moss",):
                    moss_cover(img, rng, fx, fy, fw, fh, 0.85)
                elif name in ("body", "head", "arm", "brow"):
                    moss_drips(img, rng, fx, fy, fw, fh, 4 if name == "body" else 2)
                cracks(img, rng, fx, fy, fw, fh, max(1, fw * fh // 40))
                lichen(img, rng, fx, fy, fw, fh, max(1, fw * fh // 30))

    # Face: deep-set glowing eyes and a mouth slit on the head front (9x8 at u+d, v+d).
    hx, hy = 0 + 8, 24 + 8
    for ex in (hx + 2, hx + 6):
        img.putpixel((ex - 1, hy + 3), STONE_DEEP)
        img.putpixel((ex, hy + 3), GLOW[0])
        img.putpixel((ex, hy + 4), GLOW[1])
        img.putpixel((ex - 1, hy + 4), STONE_DEEP)
    for mx in range(hx + 2, hx + 7):
        img.putpixel((mx, hy + 6), STONE_DEEP)

    # Chest: a faint glowing seam hinting at the golem core.
    bx, by = 0 + 11, 0 + 11
    for i, (dx, dy) in enumerate(((9, 4), (10, 5), (10, 6), (9, 7), (10, 8))):
        img.putpixel((bx + dx, by + dy), GLOW[i % 2])

    # Knuckles: darker fist at the lower end of the arm front.
    ax, ay = 34 + 6, 24 + 6
    for px in range(ax, ax + 6):
        for py in range(ay + 17, ay + 20):
            img.putpixel((px, py), STONE_DARK if (px + py) % 2 else STONE_DEEP)
    return img


# Rising rock: name -> (u, v, width, height, depth), must match RisingRockModel.
ROCK_BOXES = {
    "base": (0, 0, 16, 32, 16),
    "tier": (0, 48, 10, 6, 10),
    "tip": (40, 48, 4, 6, 4),
}


def rock_texture():
    rng = random.Random(99)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for name, (u, v, w, h, d) in ROCK_BOXES.items():
        for face, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            fill_stone(img, rng, fx, fy, fw, fh)
            if face in ("top", "bottom"):
                continue
            cracks(img, rng, fx, fy, fw, fh, max(2, fw * fh // 25))
            lichen(img, rng, fx, fy, fw, fh, max(1, fw * fh // 60))
            # Fresh earth clinging to the lower part of the pillar.
            if name == "base":
                for py in range(fy + fh - 6, fy + fh):
                    for px in range(fx, fx + fw):
                        if rng.random() < (py - (fy + fh - 6)) / 6:
                            img.putpixel((px, py), rng.choice(((94, 70, 50), (110, 82, 58), (80, 60, 44))))
    return img


def egg_mask():
    rows = [
        "......####......", ".....######.....", "....########....", "...##########...",
        "...##########...", "..############..", "..############..", ".##############.",
        ".##############.", ".##############.", ".##############.", "..############..",
        "..############..", "...##########...", "....########....", "......####......",
    ]
    return [[c == "#" for c in row] for row in rows]


def spawn_egg():
    rng = random.Random(7)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    mask = egg_mask()
    for y in range(16):
        for x in range(16):
            if not mask[y][x]:
                continue
            edge = any(not (0 <= x + dx < 16 and 0 <= y + dy < 16 and mask[y + dy][x + dx])
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            img.putpixel((x, y), STONE_DEEP if edge else rng.choice(STONE))
    for x, y in ((6, 3), (7, 3), (9, 5), (4, 7), (5, 7), (10, 9), (11, 9), (7, 11), (6, 12), (12, 6), (3, 10)):
        img.putpixel((x, y), rng.choice(MOSS))
    img.putpixel((8, 8), LICHEN[0])
    return img


def golem_core():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rng = random.Random(42)
    for y in range(16):
        for x in range(16):
            dist = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if dist <= 6.6:
                img.putpixel((x, y), STONE_DEEP if dist > 5.8 else rng.choice(STONE))
    for x, y in ((7, 4), (8, 5), (8, 6), (7, 7), (8, 8), (9, 9), (8, 10), (6, 7), (5, 8), (10, 6)):
        img.putpixel((x, y), GLOW[0])
    for x, y in ((7, 6), (7, 8), (8, 7), (9, 7)):
        img.putpixel((x, y), (200, 255, 245))
    return img


def icon(entity):
    # The golem's face (head front + brow) scaled up on a dark stone background.
    out = Image.new("RGBA", (128, 128), (40, 40, 40, 255))
    rng = random.Random(3)
    for y in range(0, 128, 8):
        for x in range(0, 128, 8):
            shade = rng.choice((44, 48, 52))
            for py in range(y, y + 8):
                for px in range(x, x + 8):
                    out.putpixel((px, py), (shade, shade, shade, 255))
    face = entity.crop((8, 32, 17, 40)).resize((99, 88), Image.NEAREST)
    brow = entity.crop((37, 57, 46, 59)).resize((99, 22), Image.NEAREST)
    out.alpha_composite(face, (14, 26))
    out.alpha_composite(brow, (14, 18))
    return out


def main():
    (ASSETS / "entity/stone_golem").mkdir(parents=True, exist_ok=True)
    (ASSETS / "item").mkdir(parents=True, exist_ok=True)
    entity = entity_texture()
    entity.save(ASSETS / "entity/stone_golem/stone_golem.png")
    spawn_egg().save(ASSETS / "item/stone_golem_spawn_egg.png")
    golem_core().save(ASSETS / "item/golem_core.png")
    rock_texture().save(ASSETS / "entity/rising_rock.png")
    icon(entity).save(ICON)
    print("Textures written to", ASSETS)


if __name__ == "__main__":
    main()
