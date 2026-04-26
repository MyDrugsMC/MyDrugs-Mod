from pathlib import Path
from PIL import Image
import json

# ---------- CONFIG ----------
MODID = "mydrugs"
SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent.parent
INPUT_DIR = SCRIPT_DIR / "inputs"

# Input textures
MASK_PATH = INPUT_DIR / "glass_bottle_liquid_mask.png"   # white mask
SHELL_PATH = INPUT_DIR / "glass_bottle_shell.png"        # bottle shell, only used for previews

# Output folders
OUT_TEXTURES = REPO_ROOT / "src/main/resources/assets/mydrugs/textures/item"
OUT_MODELS = REPO_ROOT / "src/main/resources/assets/mydrugs/models/item"
OUT_PREVIEWS = SCRIPT_DIR / "generated_previews"

# Output name prefix
PREFIX = "glass_bottle_liquid"

# Preview tint for ammoniac only
PREVIEW_RGB = (0xCF, 0xE1, 0x7A)

# Which stages to generate
STAGES = list(range(0, 100, 5))  # 00, 05, 10, ... 95
# ---------- /CONFIG ----------


def ensure_dirs() -> None:
    OUT_TEXTURES.mkdir(parents=True, exist_ok=True)
    OUT_MODELS.mkdir(parents=True, exist_ok=True)
    OUT_PREVIEWS.mkdir(parents=True, exist_ok=True)


def get_alpha_bbox(img: Image.Image):
    alpha = img.getchannel("A")
    return alpha.getbbox()


def make_stage(mask: Image.Image, percent: int) -> Image.Image:
    """
    Keeps only the bottom 'percent' of the non-transparent mask area.
    The cutoff row is alpha-smoothed so the stages look cleaner.
    """
    result = Image.new("RGBA", mask.size, (0, 0, 0, 0))
    bbox = get_alpha_bbox(mask)
    if bbox is None:
        return result

    left, top, right, bottom = bbox
    span = bottom - top  # bottom is exclusive
    if span <= 0:
        return result

    fill = percent / 100.0
    cutoff = bottom - (span * fill)

    src = mask.load()
    dst = result.load()

    for y in range(mask.height):
        for x in range(mask.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue

            # fully below fill line
            if y >= cutoff:
                dst[x, y] = (255, 255, 255, a)
            # boundary row: smooth alpha
            elif y + 1 > cutoff:
                frac = (y + 1) - cutoff  # 0..1
                new_a = max(0, min(255, int(a * frac)))
                if new_a > 0:
                    dst[x, y] = (255, 255, 255, new_a)

    return result


def tint_white_mask(mask_stage: Image.Image, rgb):
    """
    Applies a solid color to a white mask while preserving alpha.
    """
    tinted = Image.new("RGBA", mask_stage.size, (0, 0, 0, 0))
    src = mask_stage.load()
    dst = tinted.load()
    rr, gg, bb = rgb

    for y in range(mask_stage.height):
        for x in range(mask_stage.width):
            r, g, b, a = src[x, y]
            if a:
                dst[x, y] = (rr, gg, bb, a)

    return tinted


def write_model_json(stage_name: str):
    model = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"{MODID}:item/{stage_name}"
        }
    }

    with open(OUT_MODELS / f"{stage_name}.json", "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def write_range_dispatch_snippet():
    snippet = []
    for percent in STAGES:
        if percent == 0:
            continue
        threshold = percent / 100.0
        stage_name = f"{PREFIX}_{percent:02d}"
        snippet.append({
            "threshold": threshold,
            "model": {
                "type": "minecraft:model",
                "model": f"{MODID}:item/{stage_name}",
                "tints": [
                    {
                        "type": f"{MODID}:liquid_color",
                        "default": 16777215
                    }
                ]
            }
        })

    with open(SCRIPT_DIR / "generated_range_dispatch_entries.json", "w", encoding="utf-8") as f:
        json.dump(snippet, f, indent=2)


def main():
    ensure_dirs()

    mask = Image.open(MASK_PATH).convert("RGBA")
    shell = Image.open(SHELL_PATH).convert("RGBA") if SHELL_PATH.exists() else None

    for percent in STAGES:
        stage_name = f"{PREFIX}_{percent:02d}"

        stage = make_stage(mask, percent)
        stage.save(OUT_TEXTURES / f"{stage_name}.png")
        write_model_json(stage_name)

        if shell is not None:
            preview = shell.copy()
            tinted = tint_white_mask(stage, PREVIEW_RGB)
            preview.alpha_composite(tinted)
            preview.save(OUT_PREVIEWS / f"{stage_name}_preview.png")

    write_range_dispatch_snippet()

    print("Done.")
    print(f"Textures written to: {OUT_TEXTURES}")
    print(f"Model JSONs written to: {OUT_MODELS}")
    print("Range-dispatch snippet written to: generated_range_dispatch_entries.json")
    if shell is not None:
        print(f"Preview images written to: {OUT_PREVIEWS}")


if __name__ == "__main__":
    main()
