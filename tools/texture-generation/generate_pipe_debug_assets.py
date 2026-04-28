from pathlib import Path
import struct
import zlib

OUT = Path("src/main/resources/assets/mydrugs/textures/block/pipe")
OUT.mkdir(parents=True, exist_ok=True)

COLORS = {
    "basic_item_pipe": ((74, 96, 180, 255), (122, 148, 255, 255)),
    "fast_item_pipe": ((62, 82, 160, 255), (170, 196, 255, 255)),
    "basic_fluid_pipe": ((38, 130, 170, 255), (95, 215, 255, 255)),
    "fast_fluid_pipe": ((25, 110, 150, 255), (135, 235, 255, 255)),
    "basic_gas_pipe": ((115, 68, 170, 255), (195, 140, 255, 255)),
    "fast_gas_pipe": ((95, 50, 150, 255), (220, 170, 255, 255)),
    "pipe_debug_white": ((255, 255, 255, 255), (255, 255, 255, 255)),
}

def write_png(path: Path, pixels):
    h = len(pixels)
    w = len(pixels[0])

    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for r, g, b, a in row:
            raw.extend([r, g, b, a])

    def chunk(kind, data):
        return (
                struct.pack(">I", len(data))
                + kind
                + data
                + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
        )

    png = (
            b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
            + chunk(b"IEND", b"")
    )

    path.write_bytes(png)

def texture(base, highlight):
    img = []
    for y in range(16):
        row = []
        for x in range(16):
            # Opaque tileable-ish debug texture.
            if x in (0, 15) or y in (0, 15):
                row.append((18, 22, 30, 255))
            elif x in (1, 14) or y in (1, 14):
                row.append(highlight)
            elif (x + y) % 7 == 0:
                row.append(highlight)
            else:
                row.append(base)
        img.append(row)
    return img

for name, (base, highlight) in COLORS.items():
    write_png(OUT / f"{name}.png", texture(base, highlight))

print(f"Wrote textures to {OUT}")