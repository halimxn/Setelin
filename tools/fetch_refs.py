"""Download reference photos listed in a TSV into art/image/<key>.<ext>.

usage: python tools/fetch_refs.py refs.tsv art/image
TSV line: key<TAB>url[|fallback_url...]  — the first URL that yields a real image wins.
Reference photos are for prompting an image generator only; they are not app assets.
"""
import os
import subprocess
import sys
from io import BytesIO

from PIL import Image

tsv, out_dir = sys.argv[1], sys.argv[2]
os.makedirs(out_dir, exist_ok=True)
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"


def fetch(url):
    # curl: some image CDNs reject urllib's request fingerprint
    data = subprocess.run(["curl", "-sSfL", "--max-time", "30", "-A", UA, url], capture_output=True, check=True).stdout
    img = Image.open(BytesIO(data))
    img.load()
    return data, img


ok, bad = 0, []
for line in open(tsv, encoding="utf-8"):
    line = line.strip()
    if not line or line.startswith("#"):
        continue
    key, urls = line.split("\t")[:2]
    data = img = err = None
    for url in urls.split("|"):
        try:
            data, img = fetch(url)
            break
        except Exception as e:  # noqa: BLE001 - try the next URL
            err, img = e, None
    if img is None or img.width < 300:
        bad.append(key)
        print(f"FAIL {key:30s} {type(err).__name__ if img is None else 'too small'}: {str(err)[:70] if img is None else img.size}")
        continue
    fmt = (img.format or "PNG").lower().replace("jpeg", "jpg")
    with open(os.path.join(out_dir, f"{key}.{fmt}"), "wb") as f:
        f.write(data)
    ok += 1
    print(f"OK   {key:30s} {img.size[0]}x{img.size[1]} {len(data) // 1024} KB")
print(f"\n{ok} saved, {len(bad)} failed: {' '.join(bad)}")
