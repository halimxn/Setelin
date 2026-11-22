"""Turn every render in art/*.jpg into res/drawable-nodpi/vehicle_<key>.webp.

usage: python tools/batch_cutout.py
File names may use spaces and loose spelling ("honda hr v.jpg"); ALIASES maps them to catalog keys.
Kind fallbacks (vehicle_kind_*) are copied from a representative model.
"""
import os
import re
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ART = os.path.join(ROOT, "art")
OUT = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")

ALIASES = {  # file stem slug -> catalog key (see Catalog.kt / slug())
    "honda_stylo": "honda_stylo_160",
    "honda_vario_160_evo": "honda_vario_160",
    "suzuki_carry_pickup": "suzuki_carry_pick_up",
    "suzuki_next_ii": "suzuki_nex_ii",
    "toyota_kijang_inova_zenix": "toyota_kijang_innova_zenix",
    "yamaha_fazio": "yamaha_fazzio",
}
SKIP = {"speedometer"}
KINDS = {"matic": "honda_vario_160", "manual": "honda_supra_x_125", "sport": "yamaha_r15",
         "electric": "alva_one", "car": "toyota_avanza"}


def slug(s):
    return re.sub(r"[^a-z0-9]+", "_", s.lower()).strip("_")


done = []
for name in sorted(os.listdir(ART)):
    stem, ext = os.path.splitext(name)
    if ext.lower() not in (".jpg", ".jpeg", ".png") or slug(stem) in SKIP or stem.startswith("Gemini_"):
        continue
    key = ALIASES.get(slug(stem), slug(stem))
    dst = os.path.join(OUT, f"vehicle_{key}.webp")
    if os.path.exists(dst) and os.path.getmtime(dst) > os.path.getmtime(os.path.join(ART, name)):
        done.append(key)
        continue
    r = subprocess.run([sys.executable, os.path.join(ROOT, "tools", "cutout.py"), os.path.join(ART, name), dst, "640"],
                       capture_output=True, text=True)
    print(("OK  " if r.returncode == 0 else "ERR ") + key, r.stderr.strip()[-120:] if r.returncode else "", flush=True)
    if r.returncode == 0:
        done.append(key)

for kind, key in KINDS.items():
    src = os.path.join(OUT, f"vehicle_{key}.webp")
    if os.path.exists(src):
        shutil.copyfile(src, os.path.join(OUT, f"vehicle_kind_{kind}.webp"))
print(f"\n{len(done)} vehicles ready")
