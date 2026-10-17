#!/usr/bin/env python3
"""Generate pixel-art item and GUI textures for Rat Currency."""
from __future__ import annotations

import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ITEM = ROOT / "src" / "main" / "resources" / "assets" / "ratcurrency" / "textures" / "item"
GUI = ROOT / "src" / "main" / "resources" / "assets" / "ratcurrency" / "textures" / "gui"
SLOT = ROOT / "src" / "main" / "resources" / "assets" / "ratcurrency" / "textures" / "slot"
ITEM.mkdir(parents=True, exist_ok=True)
GUI.mkdir(parents=True, exist_ok=True)
SLOT.mkdir(parents=True, exist_ok=True)


def chunk(tag: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png(path: Path, width: int, height: int, pixels: list[tuple[int, int, int, int]]) -> None:
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            r, g, b, a = pixels[y * width + x]
            raw.extend((r, g, b, a))
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    path.write_bytes(png)


def canvas(w: int, h: int, fill=(0, 0, 0, 0)) -> list[tuple[int, int, int, int]]:
    return [fill] * (w * h)


def setp(px, w, x, y, color):
    if 0 <= x < w and 0 <= y < (len(px) // w):
        px[y * w + x] = color


def hex_color(s: str) -> tuple[int, int, int, int]:
    s = s.lstrip("#")
    return int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255


def shade(color, f):
    return tuple(max(0, min(255, int(c * f))) for c in color[:3]) + (color[3],)


def draw_coin(color: tuple[int, int, int, int]) -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    cx, cy = 7.5, 7.5
    rim = shade(color, 0.55)
    highlight = shade(color, 1.28)
    fill = shade(color, 0.92)
    shadow = shade(color, 0.7)
    square_rim = shade(color, 0.45)
    outer_r = 5.15
    inner_fill_r = 4.15

    for y in range(16):
        for x in range(16):
            dist = math.hypot(x - cx, y - cy)
            in_hole = abs(x - 7.5) <= 1.5 and abs(y - 7.5) <= 1.5
            on_square_rim = (abs(x - 7.5) <= 2.5 and abs(y - 7.5) <= 2.5) and not in_hole and (
                abs(x - 7.5) >= 1.5 or abs(y - 7.5) >= 1.5
            )
            if in_hole:
                continue
            if dist > outer_r + 0.55:
                continue
            if dist > inner_fill_r:
                col = highlight if y < 7 and x < 8 else rim
                if y > 9:
                    col = shadow
                setp(px, w, x, y, col)
            elif on_square_rim:
                setp(px, w, x, y, square_rim if y > 8 else shade(color, 0.75))
            else:
                col = fill
                if y < 6:
                    col = highlight
                elif y > 9:
                    col = shadow
                setp(px, w, x, y, col)
    return px


def draw_pouch() -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    leather = hex_color("#8B5A2B")
    dark = hex_color("#5C3A1E")
    light = hex_color("#C4A574")
    strap = hex_color("#6E4220")
    stitch = hex_color("#D2B48C")
    buckle = hex_color("#C9A227")
    black = (24, 16, 10, 255)

    # shoulder strap
    for x, y in [(2, 1), (3, 2), (4, 3), (5, 4), (11, 4), (12, 3), (13, 2), (14, 1)]:
        setp(px, w, x, y, strap)
    setp(px, w, 3, 1, black)
    setp(px, w, 13, 1, black)

    # rounded satchel body
    for y in range(5, 15):
        for x in range(2, 14):
            if (y == 5 and x in (2, 13)) or (y == 14 and x in (2, 3, 12, 13)):
                continue
            if (y == 13 and x in (2, 13)):
                continue
            col = leather
            if x <= 3 or y >= 13:
                col = dark
            if x >= 11 and y < 11:
                col = shade(leather, 1.12)
            setp(px, w, x, y, col)

    # flap
    for y in range(4, 9):
        for x in range(3, 13):
            if y == 4 and x in (3, 12):
                continue
            if y == 8 and x in (3, 12):
                continue
            col = shade(leather, 1.08 if y < 6 else 0.95)
            if x <= 4:
                col = dark
            setp(px, w, x, y, col)
    for x in range(4, 12):
        setp(px, w, x, 8, dark)

    # buckle + stitch
    setp(px, w, 7, 8, buckle)
    setp(px, w, 8, 8, buckle)
    setp(px, w, 7, 9, black)
    setp(px, w, 8, 9, black)
    setp(px, w, 5, 6, stitch)
    setp(px, w, 10, 6, stitch)
    setp(px, w, 8, 11, light)
    return px


def draw_satchel() -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    leather = hex_color("#8B5A2B")
    dark = hex_color("#5C3A1E")
    light = hex_color("#C4A574")
    strap = hex_color("#6E4220")
    stitch = hex_color("#D2B48C")
    pink = hex_color("#E8A0B0")
    fur = hex_color("#6B4A3A")
    black = (24, 16, 10, 255)
    eye = (18, 12, 8, 255)

    for x, y in [(2, 1), (3, 2), (4, 3), (5, 4), (11, 4), (12, 3), (13, 2), (14, 1)]:
        setp(px, w, x, y, strap)
    setp(px, w, 3, 1, black)
    setp(px, w, 13, 1, black)

    for y in range(5, 15):
        for x in range(2, 14):
            if (y == 5 and x in (2, 13)) or (y == 14 and x in (2, 3, 12, 13)):
                continue
            if (y == 13 and x in (2, 13)):
                continue
            col = leather
            if x <= 3 or y >= 13:
                col = dark
            if x >= 11 and y < 11:
                col = shade(leather, 1.12)
            setp(px, w, x, y, col)

    for y in range(4, 9):
        for x in range(3, 13):
            if y == 4 and x in (3, 12):
                continue
            if y == 8 and x in (3, 12):
                continue
            col = shade(leather, 1.08 if y < 6 else 0.95)
            if x <= 4:
                col = dark
            setp(px, w, x, y, col)
    for x in range(4, 12):
        setp(px, w, x, 8, dark)

    # tiny rat face on the flap
    setp(px, w, 5, 4, fur)
    setp(px, w, 10, 4, fur)
    setp(px, w, 5, 5, pink)
    setp(px, w, 10, 5, pink)
    setp(px, w, 6, 5, fur)
    setp(px, w, 7, 5, fur)
    setp(px, w, 8, 5, fur)
    setp(px, w, 9, 5, fur)
    setp(px, w, 6, 6, eye)
    setp(px, w, 9, 6, eye)
    setp(px, w, 7, 6, fur)
    setp(px, w, 8, 6, fur)
    setp(px, w, 7, 7, pink)
    setp(px, w, 8, 7, pink)
    setp(px, w, 6, 7, stitch)
    setp(px, w, 9, 7, stitch)
    setp(px, w, 7, 8, shade(pink, 0.85))
    setp(px, w, 8, 8, shade(pink, 0.85))
    setp(px, w, 8, 11, light)
    return px


def draw_empty_currency_slot() -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    # Match Curios' built-in empty-slot icons, which all use a single flat #555555.
    outline = hex_color("#555555")
    pouch = draw_pouch()
    for y in range(16):
        for x in range(16):
            if pouch[y * 16 + x][3] == 0:
                continue
            edge = False
            for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                nx, ny = x + dx, y + dy
                if nx < 0 or nx >= 16 or ny < 0 or ny >= 16 or pouch[ny * 16 + nx][3] == 0:
                    edge = True
                    break
            if edge:
                setp(px, w, x, y, outline)
    return px


def draw_case() -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    body = hex_color("#5A6470")
    dark = hex_color("#2E3540")
    light = hex_color("#8B98A8")
    latch = hex_color("#C9A227")
    black = (15, 15, 15, 255)
    for y in range(4, 14):
        for x in range(1, 15):
            col = body
            if y == 4:
                col = light
            if y >= 12:
                col = dark
            if x <= 2:
                col = dark
            setp(px, w, x, y, col)
    for x in range(1, 15):
        setp(px, w, x, 4, black)
        setp(px, w, x, 13, black)
    for y in range(4, 14):
        setp(px, w, 1, y, black)
        setp(px, w, 14, y, black)
    # lid seam
    for x in range(2, 14):
        setp(px, w, x, 8, dark)
    # handle
    for x in range(6, 10):
        setp(px, w, x, 2, black)
        setp(px, w, x, 3, light)
    setp(px, w, 5, 3, black)
    setp(px, w, 10, 3, black)
    # latches
    setp(px, w, 4, 8, latch)
    setp(px, w, 4, 9, latch)
    setp(px, w, 11, 8, latch)
    setp(px, w, 11, 9, latch)
    return px


def draw_crate() -> list[tuple[int, int, int, int]]:
    w = 16
    px = canvas(w, 16)
    tan = hex_color("#C4A574")
    dark = hex_color("#7A5A32")
    shadow = hex_color("#5A4024")
    latch = hex_color("#3A3A3A")
    metal = hex_color("#B8B8B8")
    black = (18, 14, 10, 255)
    for y in range(3, 14):
        for x in range(0, 16):
            col = tan
            if y <= 5:
                col = shade(tan, 1.08)
            if y >= 12:
                col = dark
            if x <= 1 or x >= 14:
                col = dark
            setp(px, w, x, y, col)
    for x in range(0, 16):
        setp(px, w, x, 3, black)
        setp(px, w, x, 13, black)
    for y in range(3, 14):
        setp(px, w, 0, y, black)
        setp(px, w, 15, y, black)
    # lid
    for x in range(1, 15):
        setp(px, w, x, 6, shadow)
    # corner reinforcements
    for y in range(4, 13):
        setp(px, w, 1, y, metal)
        setp(px, w, 14, y, metal)
    # latches
    for x in range(6, 10):
        setp(px, w, x, 6, latch)
        setp(px, w, x, 7, latch)
    setp(px, w, 7, 5, metal)
    setp(px, w, 8, 5, metal)
    return px


def draw_gui(width: int, height: int, tex_w: int, tex_h: int, slots: list[tuple[int, int]],
             player_x: int, player_y: int) -> list[tuple[int, int, int, int]]:
    px = canvas(tex_w, tex_h, (0, 0, 0, 0))
    bg = (198, 198, 198, 255)
    light = (255, 255, 255, 255)
    dark = (55, 55, 55, 255)
    mid = (139, 139, 139, 255)
    edge = (85, 85, 85, 255)

    def rect(x0, y0, x1, y1, color):
        for y in range(y0, y1):
            for x in range(x0, x1):
                setp(px, tex_w, x, y, color)

    rect(0, 0, width, height, bg)
    # beveled window border
    for x in range(width):
        setp(px, tex_w, x, 0, light)
        setp(px, tex_w, x, 1, light)
        setp(px, tex_w, x, height - 1, dark)
        setp(px, tex_w, x, height - 2, edge)
    for y in range(height):
        setp(px, tex_w, 0, y, light)
        setp(px, tex_w, 1, y, light)
        setp(px, tex_w, width - 1, y, dark)
        setp(px, tex_w, width - 2, y, edge)

    def draw_slot(sx, sy):
        # 18x18 frame at item position - 1
        fx, fy = sx - 1, sy - 1
        for x in range(18):
            setp(px, tex_w, fx + x, fy, dark)
            setp(px, tex_w, fx + x, fy + 17, light)
        for y in range(18):
            setp(px, tex_w, fx, fy + y, dark)
            setp(px, tex_w, fx + 17, fy + y, light)
        for y in range(1, 17):
            for x in range(1, 17):
                setp(px, tex_w, fx + x, fy + y, mid)

    for sx, sy in slots:
        draw_slot(sx, sy)
    for row in range(3):
        for col in range(9):
            draw_slot(player_x + col * 18, player_y + row * 18)
    for col in range(9):
        draw_slot(player_x + col * 18, player_y + 58)
    return px


def main():
    coins = {
        "copper_coin": hex_color("#B87333"),
        "silver_coin": hex_color("#C0C0C0"),
        "gold_coin": hex_color("#FFD700"),
        "platinum_coin": hex_color("#E5E4E2"),
        "sapphire_coin": hex_color("#0F52BA"),
        "emerald_coin": hex_color("#50C878"),
        "ruby_coin": hex_color("#E0115F"),
        "diamond_coin": hex_color("#B9F2FF"),
        "obsidian_coin": hex_color("#2A2030"),
        "netherite_coin": hex_color("#3A2A28"),
        "rat_coin": hex_color("#B44AC0"),
    }
    for name, color in coins.items():
        write_png(ITEM / f"{name}.png", 16, 16, draw_coin(color))

    write_png(ITEM / "coin_pouch.png", 16, 16, draw_pouch())
    write_png(ITEM / "coin_case.png", 16, 16, draw_case())
    write_png(ITEM / "coin_crate.png", 16, 16, draw_crate())
    write_png(ITEM / "rat_coin_satchel.png", 16, 16, draw_satchel())
    write_png(SLOT / "empty_currency_slot.png", 16, 16, draw_empty_currency_slot())

    pouch_slots = [(62 + c * 18, 17 + r * 18) for r in range(3) for c in range(3)]
    write_png(GUI / "pouch.png", 256, 256, draw_gui(176, 166, 256, 256, pouch_slots, 8, 84))

    satchel_slots = [(62 + c * 18, 17 + r * 18) for r in range(2) for c in range(3)]
    write_png(GUI / "satchel.png", 256, 256, draw_gui(176, 166, 256, 256, satchel_slots, 8, 84))

    case_slots = [(8 + c * 18, 18 + r * 18) for r in range(3) for c in range(9)]
    write_png(GUI / "case.png", 256, 256, draw_gui(176, 166, 256, 256, case_slots, 8, 84))

    crate_slots = [(20 + c * 18, 16 + r * 18) for r in range(8) for c in range(12)]
    write_png(GUI / "crate.png", 256, 256, draw_gui(256, 256, 256, 256, crate_slots, 47, 172))

    # mods.toml icon - scaled gold coin
    icon = []
    gold = draw_coin(hex_color("#FFD700"))
    for y in range(64):
        for x in range(64):
            icon.append(gold[(y // 4) * 16 + (x // 4)])
    write_png(ROOT / "src" / "main" / "resources" / "icon.png", 64, 64, icon)
    print("Textures written.")


if __name__ == "__main__":
    main()
