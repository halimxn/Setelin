"""Generates the component icon set (48x48, white glyphs, tintable) as VectorDrawables + an SVG preview sheet.

usage: python tools/icons.py app/src/main/res/drawable <preview.html>
"""
import math
import os
import sys

def C(cx, cy, r):
    return f"M{cx-r:.2f},{cy:.2f} a{r},{r} 0 1,0 {2*r},0 a{r},{r} 0 1,0 {-2*r},0 Z"

def RR(x, y, w, h, r):
    return (f"M{x+r},{y} h{w-2*r} a{r},{r} 0 0 1 {r},{r} v{h-2*r} a{r},{r} 0 0 1 {-r},{r} "
            f"h{-(w-2*r)} a{r},{r} 0 0 1 {-r},{-r} v{-(h-2*r)} a{r},{r} 0 0 1 {r},{-r} Z")

def P(*pts):
    return "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + " Z"

def polar(cx, cy, r, a):
    return cx + r * math.cos(a), cy + r * math.sin(a)

def gear(cx, cy, ro, ri, teeth, hole):
    pts = []
    n = teeth * 4
    for i in range(n):
        a = 2 * math.pi * i / n - math.pi / 2
        r = ro if (i % 4) in (1, 2) else ri
        pts.append(polar(cx, cy, r, a))
    return P(*pts) + " " + C(cx, cy, hole)

def drop(cx, top, h):
    w = h * 0.62
    return (f"M{cx},{top} C{cx},{top} {cx-w/2},{top+h*0.48} {cx-w/2},{top+h*0.68} "
            f"a{w/2},{w/2} 0 0 0 {w},0 C{cx+w/2},{top+h*0.48} {cx},{top} {cx},{top} Z")

F = lambda d: {"d": d}                              # filled, non-zero
E = lambda d: {"d": d, "eo": True}                  # filled, even-odd
S = lambda d, w=3.2: {"d": d, "stroke": w}          # stroked line

ICONS = {
    # brake disc: drilled rotor with hub
    "disc": [E(C(24, 24, 20) + " " + C(24, 24, 7) + " " + " ".join(
        C(*polar(24, 24, 14, i * math.pi / 4), 1.9) for i in range(8)))],
    # brake pad: friction block with slot and mounting ears
    "brake": [E("M18,5 h12 c3.3,0 6,2.7 6,6 v8 c0,1.6 -0.8,3 -2,4 c1.2,1 2,2.4 2,4 v10 c0,3.3 -2.7,6 -6,6 h-12 "
                "c-3.3,0 -6,-2.7 -6,-6 v-10 c0,-1.6 0.8,-3 2,-4 c-1.2,-1 -2,-2.4 -2,-4 v-8 c0,-3.3 2.7,-6 6,-6 Z "
                + RR(22.2, 10, 3.6, 28, 1.8)),
              F(RR(7, 20, 5, 8, 1.5)), F(RR(36, 20, 5, 8, 1.5))],
    # brake fluid: reservoir with liquid wave and brackets
    "fluid": [E(C(24, 24, 14) + " " + C(24, 24, 11)),
              F("M13.6,26 c3,-2.4 6.4,-2.4 10.4,0 c4,2.4 7.4,2.4 10.4,0 a10.6,10.6 0 0 1 -20.8,0 Z"),
              S("M8.5,13 a17,17 0 0 0 0,22", 3), S("M39.5,13 a17,17 0 0 1 0,22", 3)],
    # engine oil: oil can with spout and drop
    "oil": [F(RR(4, 20, 26, 16, 3)),
            F(P((29, 22), (42, 14.5), (44.5, 17.5), (31, 31))),
            F(RR(13, 15, 7, 6, 1.2)), F(RR(9, 12, 15, 3.4, 1.7)),
            F(drop(43.5, 23, 11))],
    # oil filter canister
    "filter": [E(RR(12, 12, 24, 30, 4) + " " + RR(12, 21, 24, 2.4, 0) + " " + RR(12, 27, 24, 2.4, 0)
                 + " " + RR(12, 33, 24, 2.4, 0)),
               F(RR(15, 6, 18, 5, 1.5))],
    # fuel / funnel with drop
    "fuel": [F(P((5, 7), (43, 7), (28, 24), (28, 31), (20, 31), (20, 24))), F(drop(24, 33, 13))],
    # tyre: tread ring with rim and hub
    "tire": [E(C(24, 24, 21) + " " + C(24, 24, 13.5) + " " + " ".join(
        P(polar(24, 24, 21.6, a - 0.09), polar(24, 24, 21.6, a + 0.09),
          polar(24, 24, 17.2, a + 0.07), polar(24, 24, 17.2, a - 0.07))
        for a in [i * math.pi / 6 for i in range(12)])),
             E(C(24, 24, 10) + " " + C(24, 24, 3.4) + " " + " ".join(
                 C(*polar(24, 24, 6.8, i * 2 * math.pi / 5 - math.pi / 2), 1.5) for i in range(5)))],
    # air filter: pleated panel
    "air": [E(RR(5, 10, 38, 28, 5) + " " + RR(9, 14, 30, 20, 2)),
            S("M11,32 L15,16 L19,32 L23,16 L27,32 L31,16 L35,32 L37,24", 2.6)],
    # 12V battery with + and -
    "battery": [E(RR(5, 14, 38, 27, 4) + " " + P((13, 26), (16, 26), (16, 23), (19, 23), (19, 26), (22, 26),
                                                 (22, 29), (19, 29), (19, 32), (16, 32), (16, 29), (13, 29))
                  + " " + RR(27, 26, 9, 3, 0)),
                F(RR(10, 9, 7, 5, 1.2)), F(RR(31, 9, 7, 5, 1.2))],
    # coolant: thermometer in liquid
    "coolant": [E(RR(19, 3, 10, 26, 5) + " " + RR(22, 6, 4, 20, 2)), F(C(24, 31, 7.5)),
                F(RR(22.6, 14, 2.8, 14, 1.2)),
                S("M4,42 c3.3,-2.6 6.7,-2.6 10,0 c3.3,2.6 6.7,2.6 10,0 c3.3,-2.6 6.7,-2.6 10,0 c3.3,2.6 6.7,2.6 10,0", 2.6),
                S("M6,26 h7 M35,26 h7", 2.6)],
    # CVT belt: two pulleys and the belt loop
    "belt": [E(C(15, 24, 11.5) + " " + C(15, 24, 8)), E(C(36, 24, 7.5) + " " + C(36, 24, 4.3)),
             F(C(15, 24, 3.4)), F(C(36, 24, 2)),
             S("M14.2,12.5 L35.5,16.5", 3), S("M14.2,35.5 L35.5,31.5", 3)],
    # roller weights: three rollers
    "roller": [E(C(15, 17, 9) + " " + C(15, 17, 3.4)), E(C(33, 17, 9) + " " + C(33, 17, 3.4)),
               E(C(24, 33, 9) + " " + C(24, 33, 3.4))],
    # CVT spring: coil spring
    "spring": [F(RR(14, 4, 20, 4.5, 2)), F(RR(14, 39.5, 20, 4.5, 2)),
               S("M16,11 L32,15 L16,19 L32,23 L16,27 L32,31 L16,35 L32,37", 3.2)],
    # shock absorber
    "shock": [E(C(24, 7, 5) + " " + C(24, 7, 2)), E(C(24, 41, 5) + " " + C(24, 41, 2)),
              F(RR(20, 13, 8, 24, 2)),
              S("M14,16 L34,19 M14,21 L34,24 M14,26 L34,29 M14,31 L34,34", 3)],
    # spark plug
    "plug": [F(RR(21, 2, 6, 6, 1.2)), F(RR(18.5, 7, 11, 15, 4)), F(P((15, 22), (33, 22), (33, 28), (15, 28))),
             E(RR(18, 28, 12, 10, 1) + " " + RR(18, 30.8, 12, 1.6, 0) + " " + RR(18, 34, 12, 1.6, 0)),
             F(RR(22.6, 38, 2.8, 5, 1)), S("M24,45.5 h5 v-3", 2.4)],
    # transmission / gear oil: gear with a drop
    "gear": [E(gear(20, 21, 16, 12, 8, 5)), F(drop(39, 28, 15))],
    # CVT cleaning: pulley with sparkles
    "cvt": [E(C(20, 27, 15) + " " + C(20, 27, 4) + " " + " ".join(
        P(polar(20, 27, 6.5, a - 0.38), polar(20, 27, 12, a - 0.26), polar(20, 27, 12, a + 0.26), polar(20, 27, 6.5, a + 0.38))
        for a in [i * 2 * math.pi / 5 for i in range(5)])),
            F("M38,3 l2,5.5 l5.5,2 l-5.5,2 l-2,5.5 l-2,-5.5 l-5.5,-2 l5.5,-2 Z"),
            F("M41,22 l1.2,3 l3,1.2 l-3,1.2 l-1.2,3 l-1.2,-3 l-3,-1.2 l3,-1.2 Z")],
    # throttle body cleaner: spray can
    "water": [F(RR(10, 16, 18, 28, 3.5)), F(RR(13, 10, 12, 6, 1.5)), F(RR(16, 5, 8, 5, 1.2)),
              F(C(31, 7, 1.8)), F(C(36, 4, 1.8)), F(C(36, 10, 1.8)), F(C(41, 7, 1.8)), F(C(41, 13, 1.8)), F(C(41, 1.5, 1.6))],
    # periodic service / valve: wrench
    "tune": [F("M36.5,4.5 a9,9 0 0 0 -11.3,11.6 L6.8,34.5 a3.8,3.8 0 0 0 0,5.4 l1.3,1.3 a3.8,3.8 0 0 0 5.4,0 "
               "L31.9,22.8 a9,9 0 0 0 11.6,-11.3 l-5.5,5.5 l-5.4,-1.2 l-1.2,-5.4 Z")],
    # wheel bearing: two races with balls
    "bearing": [E(C(24, 24, 21) + " " + C(24, 24, 15.5)), E(C(24, 24, 8.5) + " " + C(24, 24, 4.5)),
                F(" ".join(C(*polar(24, 24, 12, i * math.pi / 4), 2.8) for i in range(8)))],
    # paperwork
    "doc": [E("M10,4 h18 l10,10 v28 a3,3 0 0 1 -3,3 h-25 a3,3 0 0 1 -3,-3 v-35 a3,3 0 0 1 3,-3 Z "
              + RR(14, 22, 20, 3, 1.5) + " " + RR(14, 29, 20, 3, 1.5) + " " + RR(14, 36, 13, 3, 1.5)),
            F("M28,4 v8 a2,2 0 0 0 2,2 h8 Z")],
    "plate": [E(RR(3, 12, 42, 24, 4) + " " + RR(6.5, 15.5, 35, 17, 2)), F(RR(11, 20, 26, 3, 1.5)), F(RR(11, 26, 26, 3, 1.5))],
    "license": [E(RR(3, 9, 42, 30, 4) + " " + RR(6.5, 12.5, 35, 23, 2)), F(C(16, 20, 4)),
                F("M9.5,32 a6.5,6 0 0 1 13,0 Z"), F(RR(26, 18, 12, 3, 1.5)), F(RR(26, 24, 12, 3, 1.5)), F(RR(26, 30, 8, 3, 1.5))],
    "shield": [E("M24,3 L41,9 v12 c0,11 -7.4,19.6 -17,23 c-9.6,-3.4 -17,-12 -17,-23 v-12 Z "
                 "M24,7 L37,11.6 v9.4 c0,8.8 -5.6,15.8 -13,18.8 c-7.4,-3 -13,-10 -13,-18.8 v-9.4 Z"),
               S("M16.5,23.5 L22,29 L32,18.5", 3.4)],
    "emission": [F(RR(3, 30, 24, 9, 3)), F(RR(25, 28, 6, 13, 2)),
                 F(C(36, 22, 5)), F(C(41, 13, 4)), F(C(34, 9, 3))],
    "service": [F("M36.5,4.5 a9,9 0 0 0 -11.3,11.6 L6.8,34.5 a3.8,3.8 0 0 0 0,5.4 l1.3,1.3 a3.8,3.8 0 0 0 5.4,0 "
                  "L31.9,22.8 a9,9 0 0 0 11.6,-11.3 l-5.5,5.5 l-5.4,-1.2 l-1.2,-5.4 Z")],
}


def xml(shapes):
    out = ['<?xml version="1.0" encoding="utf-8"?>',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="48dp" android:height="48dp" '
           'android:viewportWidth="48" android:viewportHeight="48">']
    for s in shapes:
        if "stroke" in s:
            out.append(f'    <path android:strokeColor="#FFFFFFFF" android:strokeWidth="{s["stroke"]}" android:strokeLineCap="round" '
                       f'android:strokeLineJoin="round" android:pathData="{s["d"]}" />')
        else:
            ft = ' android:fillType="evenOdd"' if s.get("eo") else ""
            out.append(f'    <path android:fillColor="#FFFFFFFF"{ft} android:pathData="{s["d"]}" />')
    out.append("</vector>")
    return "\n".join(out) + "\n"


def svg(shapes, color="#A0A0A5"):
    parts = []
    for s in shapes:
        if "stroke" in s:
            parts.append(f'<path d="{s["d"]}" fill="none" stroke="{color}" stroke-width="{s["stroke"]}" stroke-linecap="round" stroke-linejoin="round"/>')
        else:
            parts.append(f'<path d="{s["d"]}" fill="{color}" fill-rule="{"evenodd" if s.get("eo") else "nonzero"}"/>')
    return f'<svg viewBox="0 0 48 48" width="96" height="96">{"".join(parts)}</svg>'


if __name__ == "__main__":
    res_dir, preview = sys.argv[1], sys.argv[2]
    for key, shapes in ICONS.items():
        with open(os.path.join(res_dir, f"ic_comp_{key}.xml"), "w") as f:
            f.write(xml(shapes))
    cells = "".join(
        f'<div class="c">{svg(s, "#00CA48" if i % 5 == 0 else "#A0A0A5")}<span>{k}</span></div>'
        for i, (k, s) in enumerate(ICONS.items()))
    open(preview, "w").write(
        '<!doctype html><meta charset="utf-8"><title>icons</title><style>body{background:#08090B;margin:0;padding:8px;'
        'display:grid;grid-template-columns:repeat(5,1fr);gap:8px;font:12px sans-serif;color:#aaa}'
        '.c{background:#1C1C1E;border-radius:18px;display:flex;flex-direction:column;align-items:center;padding:10px}</style>'
        + cells)
    print(len(ICONS), "icons")
