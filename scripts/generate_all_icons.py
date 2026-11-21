#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

PROJECT_ROOT = "/home/jasmin/Projects/wirefm"
RES_DIR = os.path.join(PROJECT_ROOT, "android/app/src/main/res")
DRAWABLE_DIR = os.path.join(RES_DIR, "drawable")
DESKTOP_DIR = os.path.join(PROJECT_ROOT, "desktop")
SERVER_STATIC_DIR = os.path.join(PROJECT_ROOT, "server/static")

def draw_mark(canvas_size, glyph_size, fg_color, bg_color=None, squircle_radius=None, is_circle=False):
    """Draws pixel-perfect centered 3-layer WireFM icon glyph using supersampling."""
    scale = 4
    s = canvas_size * scale
    g = glyph_size * scale
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    if bg_color is not None:
        if is_circle:
            draw.ellipse([0, 0, s - 1, s - 1], fill=bg_color)
        elif squircle_radius is not None:
            r = int(squircle_radius * scale)
            draw.rounded_rectangle([0, 0, s - 1, s - 1], radius=r, fill=bg_color)
        else:
            draw.rectangle([0, 0, s - 1, s - 1], fill=bg_color)

    # Base coords in 512x512 space, with +31 Y-shift for exact vertical centering
    # Bounding box: X in [102, 410] (width 308), Y in [89, 423] (height 334). Center is exactly (256, 256).
    pts_top = [(256, 109), (390, 185), (256, 261), (122, 185)]
    pts_mid = [(122, 257), (256, 333), (390, 257)]
    pts_bot = [(122, 327), (256, 403), (390, 327)]

    factor = g / 334.0
    lw = max(1, int(40 * (g / 512.0)))
    half_lw = lw // 2
    cx, cy = s // 2, s // 2

    def transform(x, y):
        nx = cx + int((x - 256) * factor)
        ny = cy + int((y - 256) * factor)
        return (nx, ny)

    scaled_top = [transform(x, y) for x, y in pts_top]
    scaled_mid = [transform(x, y) for x, y in pts_mid]
    scaled_bot = [transform(x, y) for x, y in pts_bot]

    # Draw lines with round joints
    draw.line(scaled_top + [scaled_top[0]], fill=fg_color, width=lw, joint="round")
    draw.line(scaled_mid, fill=fg_color, width=lw, joint="round")
    draw.line(scaled_bot, fill=fg_color, width=lw, joint="round")

    # Round caps on all vertices
    for pts in [scaled_top, scaled_mid, scaled_bot]:
        for x, y in pts:
            draw.ellipse([x - half_lw, y - half_lw, x + half_lw, y + half_lw], fill=fg_color)

    return img.resize((canvas_size, canvas_size), Image.Resampling.LANCZOS)

def main():
    print("Generating WireFM icons with exact center alignment...")

    # 1. Drawable tiles (512x512)
    os.makedirs(DRAWABLE_DIR, exist_ok=True)
    tile_white = draw_mark(512, int(512 * 0.62), "#111111", bg_color="#FFFFFF", squircle_radius=112)
    tile_dark = draw_mark(512, int(512 * 0.62), "#FFFFFF", bg_color="#111111", squircle_radius=112)

    tile_white.save(os.path.join(DRAWABLE_DIR, "wirefm_tile_white.png"), "PNG")
    tile_dark.save(os.path.join(DRAWABLE_DIR, "wirefm_tile_dark.png"), "PNG")
    print("  ✓ Saved wirefm_tile_white.png and wirefm_tile_dark.png")

    # 2. Desktop appicon.png (512x512)
    os.makedirs(DESKTOP_DIR, exist_ok=True)
    tile_dark.save(os.path.join(DESKTOP_DIR, "appicon.png"), "PNG")
    print("  ✓ Saved desktop/appicon.png")

    # 3. Server static assets
    os.makedirs(SERVER_STATIC_DIR, exist_ok=True)
    tile_dark.save(os.path.join(SERVER_STATIC_DIR, "logo.png"), "PNG")
    tile_dark.save(os.path.join(SERVER_STATIC_DIR, "favicon.ico"), format="ICO", sizes=[(16,16), (32,32), (48,48), (64,64)])
    print("  ✓ Saved server/static logo & favicon")

    # 4. Android Mipmap densities
    densities = {
        "mipmap-mdpi": (48, 108),
        "mipmap-hdpi": (72, 162),
        "mipmap-xhdpi": (96, 216),
        "mipmap-xxhdpi": (144, 324),
        "mipmap-xxxhdpi": (192, 432)
    }

    for folder, (legacy_size, adaptive_size) in densities.items():
        target_dir = os.path.join(RES_DIR, folder)
        os.makedirs(target_dir, exist_ok=True)

        # A) Adaptive Foreground (dark glyph centered in transparent canvas, 44% size -> fits safe circle)
        fg_glyph_size = int(adaptive_size * 0.44)
        fg = draw_mark(adaptive_size, fg_glyph_size, "#111111")
        fg.save(os.path.join(target_dir, "ic_launcher_foreground.png"), "PNG")

        # B) Adaptive Monochrome (white glyph centered in transparent canvas)
        mono = draw_mark(adaptive_size, fg_glyph_size, "#FFFFFF")
        mono.save(os.path.join(target_dir, "ic_launcher_monochrome.png"), "PNG")

        # C) Legacy ic_launcher.png (White squircle tile with dark glyph)
        legacy_radius = int(legacy_size * 0.22)
        legacy_glyph_size = int(legacy_size * 0.58)
        legacy = draw_mark(legacy_size, legacy_glyph_size, "#111111", bg_color="#FFFFFF", squircle_radius=legacy_radius)
        legacy.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")

        # D) Legacy Round ic_launcher_round.png (White circle with dark glyph)
        round_glyph_size = int(legacy_size * 0.54)
        round_icon = draw_mark(legacy_size, round_glyph_size, "#111111", bg_color="#FFFFFF", is_circle=True)
        round_icon.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")

        print(f"  ✓ Generated mipmap icons for {folder}")

    print("All icons successfully generated!")

if __name__ == "__main__":
    main()
