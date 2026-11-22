"""Cut a product render out of a flat white background and save a trimmed WebP with alpha.

usage: python tools/cutout.py art/<file>.jpg app/src/main/res/drawable-nodpi/vehicle_<key>.webp [width=640]
The background is the near-white region connected to the image border (flood fill), so bright
highlights inside the vehicle stay opaque. Edge pixels are un-mixed from white to avoid halos.
"""
import sys
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

src, dst = sys.argv[1], sys.argv[2]
width = int(sys.argv[3]) if len(sys.argv) > 3 else 640

im = Image.open(src).convert("RGB")
im.thumbnail((1400, 1400), Image.LANCZOS)
W, H = im.size

work = im.copy()
KEY = (255, 0, 255)
for xy in [(0, 0), (W - 1, 0), (0, H - 1), (W - 1, H - 1), (W // 2, 0), (W // 2, H - 1), (0, H // 2), (W - 1, H // 2)]:
    if work.getpixel(xy) != KEY:
        ImageDraw.floodfill(work, xy, KEY, thresh=26)
a = np.asarray(work)
bg = (a[..., 0] == 255) & (a[..., 1] == 0) & (a[..., 2] == 255)

# Enclosed pockets of background (between spokes, under the seat): flat near-white regions
# that are big enough to be background rather than a highlight or an LED.
src_np = np.asarray(im).astype(np.int16)
near = (src_np.min(axis=2) > 238) & ((src_np.max(axis=2) - src_np.min(axis=2)) < 12) & ~bg
visited = np.zeros_like(near)
pockets = 0
while True:
    cand = np.argwhere(near & ~visited)
    if len(cand) == 0:
        break
    y, x = cand[0]
    probe = im.copy()
    ImageDraw.floodfill(probe, (int(x), int(y)), KEY, thresh=14)
    pa = np.asarray(probe)
    region = (pa[..., 0] == 255) & (pa[..., 1] == 0) & (pa[..., 2] == 255)
    if not region.any():
        region = np.zeros_like(near)
        region[y, x] = True
    visited |= region
    if region.sum() >= 60:
        bg |= region
        pockets += 1
print("enclosed pockets removed:", pockets)

# soft alpha: 1 inside, 0 background, feathered edge
mask = Image.fromarray(((~bg) * 255).astype(np.uint8))
mask = mask.filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.9))
alpha = np.asarray(mask).astype(np.float32) / 255.0

rgb = np.asarray(im).astype(np.float32)
# un-mix white from semi-transparent edge pixels
al = np.clip(alpha, 1e-3, 1)[..., None]
rgb = np.clip((rgb - (1 - al) * 255.0) / al, 0, 255)

out = np.dstack([rgb, alpha * 255]).astype(np.uint8)
img = Image.fromarray(out, "RGBA")
bbox = img.getchannel("A").point(lambda v: 255 if v > 8 else 0).getbbox()
img = img.crop(bbox)
pad = int(img.width * 0.02)
canvas = Image.new("RGBA", (img.width + 2 * pad, img.height + 2 * pad), (0, 0, 0, 0))
canvas.paste(img, (pad, pad))
canvas.thumbnail((width, width), Image.LANCZOS)
canvas.save(dst, "WEBP", quality=88, method=6)
print("saved", dst, canvas.size)
