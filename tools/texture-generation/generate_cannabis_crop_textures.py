from PIL import Image, ImageDraw, ImageFilter
import math, random, os, zipfile, textwrap
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
outdir = SCRIPT_DIR / "generated" / "cannabis_crop_textures"
os.makedirs(outdir, exist_ok=True)

SCRIPT = r'''
from PIL import Image, ImageDraw, ImageFilter
import math, random, os

OUTDIR = "cannabis_crop_textures"
os.makedirs(OUTDIR, exist_ok=True)

SCALE = 8
SIZE = 32
HI = SIZE * SCALE

def rgba(hex_str, a=255):
    hex_str = hex_str.lstrip("#")
    return tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4)) + (a,)

BG = (0, 0, 0, 0)
STEM = rgba("#58733A")
STEM_DARK = rgba("#40592A")
LEAF = rgba("#5F8F3A")
LEAF_MID = rgba("#6FA544")
LEAF_LIGHT = rgba("#81B85A")
BUD = rgba("#8AAE49")
BUD_LIGHT = rgba("#A5C26A")
HAIR = rgba("#C57A45", 150)
SOIL = rgba("#6E4F33")
SOIL_DARK = rgba("#573C25")
HIGHLIGHT = (255, 255, 255, 22)

def leaf_polygon(cx, cy, length, width, angle_deg):
    ang = math.radians(angle_deg)
    dx = math.cos(ang)
    dy = math.sin(ang)
    px = -dy
    py = dx

    pts = []
    steps = 10

    for i in range(steps + 1):
        t = i / steps
        r = math.sin(math.pi * t) ** 0.75
        taper = (0.24 + 0.9 * (1 - abs(2 * t - 1))) * width * r
        bx = cx + dx * (t * length)
        by = cy + dy * (t * length)
        pts.append((bx + px * taper, by + py * taper))

    for i in range(steps, -1, -1):
        t = i / steps
        r = math.sin(math.pi * t) ** 0.75
        taper = (0.24 + 0.9 * (1 - abs(2 * t - 1))) * width * r
        bx = cx + dx * (t * length)
        by = cy + dy * (t * length)
        pts.append((bx - px * taper, by - py * taper))

    return pts

def draw_leaf(draw, cx, cy, length, width, angle, fill, outline=None, vein=(255,255,255,32)):
    poly = leaf_polygon(cx, cy, length, width, angle)
    draw.polygon(poly, fill=fill, outline=outline)
    ang = math.radians(angle)
    x2 = cx + math.cos(ang) * length
    y2 = cy + math.sin(ang) * length
    draw.line((cx, cy, x2, y2), fill=vein, width=max(1, int(width * 0.22)))

def draw_palmate(draw, x, y, leaf_len, width, facing=-1, spread=34, colors=(LEAF, LEAF_MID, LEAF_LIGHT)):
    base_angles = [0, spread*0.55, spread, -spread*0.55, -spread]
    angles = [180 + facing*a for a in base_angles]
    cols = [colors[2], colors[1], colors[0], colors[1], colors[2]]
    lens = [leaf_len*0.72, leaf_len*0.88, leaf_len, leaf_len*0.88, leaf_len*0.72]
    widths = [width*0.9, width, width*1.08, width, width*0.9]
    for a, c, L, W in zip(angles, cols, lens, widths):
        draw_leaf(draw, x, y, L, W, a, c)

def draw_bud(draw, x, y, r, dense=1.0):
    for i in range(int(6 * dense)):
        ox = (i % 3 - 1) * r * 0.32 + (0.2 if i % 2 else -0.2)
        oy = (i // 3 - 1) * r * 0.28
        draw.ellipse((x-r+ox, y-r+oy, x+r+ox, y+r+oy), fill=BUD)
    draw.ellipse((x-r*0.8, y-r*0.8, x+r*0.8, y+r*0.8), fill=BUD_LIGHT)
    draw.line((x-r*0.4, y-r*0.2, x+r*0.35, y+r*0.15), fill=HAIR, width=max(1, int(r*0.16)))
    draw.line((x-r*0.15, y+r*0.15, x+r*0.45, y-r*0.25), fill=HAIR, width=max(1, int(r*0.12)))

def stage_params(stage):
    return [
        dict(nodes=1, height=8, top=24, leaf_len=5.5, leaf_w=1.6, bud=0.0, side=0),
        dict(nodes=2, height=11, top=22, leaf_len=7.0, leaf_w=1.8, bud=0.0, side=1),
        dict(nodes=3, height=15, top=20, leaf_len=9.0, leaf_w=2.2, bud=0.0, side=1),
        dict(nodes=4, height=18, top=18, leaf_len=10.6, leaf_w=2.5, bud=0.0, side=1),
        dict(nodes=5, height=21, top=16, leaf_len=11.8, leaf_w=2.7, bud=0.55, side=1),
        dict(nodes=6, height=24, top=13, leaf_len=12.4, leaf_w=2.8, bud=0.9, side=1),
        dict(nodes=6, height=26, top=11, leaf_len=13.2, leaf_w=2.9, bud=1.15, side=1),
    ][stage]

def make_stage(stage, seed=7):
    rnd = random.Random(seed + stage * 11)
    img = Image.new("RGBA", (HI, HI), BG)
    draw = ImageDraw.Draw(img, "RGBA")

    # soft ground hint for rooted look
    ground_y = int(HI * 0.90)
    draw.ellipse((HI*0.31, ground_y-6*SCALE, HI*0.69, ground_y+2*SCALE), fill=(SOIL[0], SOIL[1], SOIL[2], 38))
    draw.arc((HI*0.34, ground_y-5*SCALE, HI*0.66, ground_y+1*SCALE), 0, 180, fill=(SOIL_DARK[0], SOIL_DARK[1], SOIL_DARK[2], 45), width=2*SCALE)

    p = stage_params(stage)
    cx = HI // 2
    base_y = int(HI * 0.84)
    top_y = int(HI * (p["top"] / 32.0))
    stem_w = max(2, int((1.15 + stage * 0.11) * SCALE))

    # main stem
    draw.line((cx, base_y, cx, top_y), fill=STEM_DARK, width=stem_w + SCALE//2)
    draw.line((cx, base_y, cx, top_y), fill=STEM, width=stem_w)

    # nodes / side leaves
    if p["nodes"] > 0:
        for i in range(p["nodes"]):
            t = 0 if p["nodes"] == 1 else i / (p["nodes"] - 1)
            y = int(base_y - (base_y - top_y) * (0.10 + 0.78 * t))
            x = cx + int(math.sin((t + 0.12) * 2.9) * SCALE * 0.5)

            # side branches
            if stage >= 2 and i < p["nodes"] - 1:
                branch_len = (2.2 + stage * 0.7 + i * 0.18) * SCALE
                left_end = (x - branch_len * 0.66, y + branch_len * 0.12)
                right_end = (x + branch_len * 0.66, y + branch_len * 0.10)
                draw.line((x, y, *left_end), fill=STEM_DARK, width=max(2, stem_w // 2))
                draw.line((x, y, *right_end), fill=STEM_DARK, width=max(2, stem_w // 2))

            leaf_len = (p["leaf_len"] * SCALE) * (0.70 + 0.35 * t)
            leaf_w = p["leaf_w"] * SCALE

            if stage == 0:
                draw_leaf(draw, x, y, leaf_len*0.65, leaf_w*0.95, 210, LEAF_MID)
                draw_leaf(draw, x, y, leaf_len*0.65, leaf_w*0.95, -30, LEAF_MID)
            else:
                draw_palmate(draw, x - SCALE*0.2, y + SCALE*0.2, leaf_len, leaf_w, facing=1)
                draw_palmate(draw, x + SCALE*0.2, y + SCALE*0.2, leaf_len, leaf_w, facing=-1)

            if p["bud"] > 0 and i >= max(1, p["nodes"] - 3):
                bud_r = (1.1 + p["bud"] + i * 0.05) * SCALE
                draw_bud(draw, x, y - SCALE*0.4, bud_r, dense=0.7 + p["bud"])

    # top crown
    crown_len = p["leaf_len"] * SCALE * (0.82 + 0.06 * stage)
    crown_w = p["leaf_w"] * SCALE * 1.03
    crown_y = top_y + SCALE * 0.7
    draw_palmate(draw, cx, crown_y, crown_len, crown_w, facing=1, spread=30)
    draw_palmate(draw, cx, crown_y, crown_len, crown_w, facing=-1, spread=30)

    # top cola on later stages
    if p["bud"] > 0:
        draw_bud(draw, cx, top_y - SCALE*0.6, (1.8 + p["bud"] * 1.7) * SCALE, dense=1.0 + p["bud"])
        if stage >= 5:
            draw_bud(draw, cx, top_y + SCALE*1.1, (1.25 + p["bud"] * 1.1) * SCALE, dense=0.85 + p["bud"] * 0.6)

    # small highlight wash for more volume
    highlight = Image.new("RGBA", (HI, HI), (0,0,0,0))
    hd = ImageDraw.Draw(highlight, "RGBA")
    hd.ellipse((HI*0.28, HI*0.08, HI*0.62, HI*0.85), fill=(255,255,255,14))
    highlight = highlight.filter(ImageFilter.GaussianBlur(radius=3*SCALE))
    img = Image.alpha_composite(img, highlight)

    # slight blur + downscale for smoother pixel-art texture
    img = img.filter(ImageFilter.GaussianBlur(radius=0.45*SCALE))
    img = img.resize((SIZE, SIZE), Image.Resampling.LANCZOS)

    # subtle pixel cleanup / contrast
    px = img.load()
    for yy in range(SIZE):
        for xx in range(SIZE):
            r,g,b,a = px[xx,yy]
            if a < 12:
                px[xx,yy] = (0,0,0,0)
            elif a < 64:
                px[xx,yy] = (r,g,b,int(a*0.85))

    return img

def generate():
    for stage in range(7):
        img = make_stage(stage)
        img.save(os.path.join(OUTDIR, f"cannabis_stage_{stage}.png"))

if __name__ == "__main__":
    generate()
'''

script_path = os.path.join(outdir, "generate_cannabis_textures.py")
with open(script_path, "w", encoding="utf-8") as f:
    f.write(SCRIPT)

# run generator from the same code
namespace = {}
exec(SCRIPT, namespace)
namespace["OUTDIR"] = outdir
namespace["generate"]()

# Make preview sheet
images = [Image.open(os.path.join(outdir, f"cannabis_stage_{i}.png")).convert("RGBA") for i in range(7)]
margin = 8
label_h = 16
cell_w = 32 + margin*2
sheet_w = cell_w * 7
sheet_h = 32 + label_h + margin*2
sheet = Image.new("RGBA", (sheet_w, sheet_h), (24, 24, 24, 255))
draw = ImageDraw.Draw(sheet)
for i, img in enumerate(images):
    x = i * cell_w + margin
    y = margin
    # checker bg
    for yy in range(0, 32, 8):
        for xx in range(0, 32, 8):
            c = 70 if (xx//8 + yy//8) % 2 == 0 else 56
            draw.rectangle((x+xx, y+yy, x+xx+7, y+yy+7), fill=(c, c, c, 255))
    sheet.alpha_composite(img, (x, y))
    draw.text((x, y + 34), f"Stage {i+1}", fill=(230, 230, 230, 255))
preview_path = SCRIPT_DIR / "generated" / "cannabis_crop_preview.png"
sheet.save(preview_path)

# zip files
zip_path = SCRIPT_DIR / "generated" / "cannabis_crop_textures.zip"
with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
    for i in range(7):
        zf.write(os.path.join(outdir, f"cannabis_stage_{i}.png"), arcname=f"cannabis_stage_{i}.png")
    zf.write(script_path, arcname="generate_cannabis_textures.py")

print("Created:")
print(preview_path)
print(zip_path)
print(script_path)
