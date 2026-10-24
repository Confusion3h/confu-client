"""Writes the hand-authored confusione SVGs (geometry spelled out in docs/branding.md)
and renders PNGs with cairosvg. Run: python3 branding/gen.py"""
import math, os
import cairosvg

HERE = os.path.dirname(os.path.abspath(__file__))
CX = CY = 128
GRAD = '''<defs>
    <linearGradient id="cg" x1="28" y1="28" x2="228" y2="228" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="#7C5CFF"/>
      <stop offset="0.55" stop-color="#FF4FA3"/>
      <stop offset="1" stop-color="#FFB547"/>
    </linearGradient>
  </defs>'''

def pt(r, deg):
    a = math.radians(deg)
    return round(CX + r * math.cos(a), 2), round(CY + r * math.sin(a), 2)

def c_path(r, tail_r, tail_end_r):
    # 280 deg arc open to the right (y-down angles -40..+40), upper terminal curls inward
    p_lo, p_hi = pt(r, 40), pt(r, -40)
    t = pt(tail_end_r, -66)
    return (f"M {p_lo[0]} {p_lo[1]} A {r} {r} 0 1 1 {p_hi[0]} {p_hi[1]} "
            f"A {tail_r} {tail_r} 0 0 0 {t[0]} {t[1]}")

def svg(stroke, dot, bg=None, ring=True, small=False):
    if small:  # <=32 px: no ring, bigger and bolder C
        r, sw, tail_r, tail_end, dot_r = 84, 34, 40, 44, 17
    else:
        r, sw, tail_r, tail_end, dot_r = 62, 20, 30, 34, 11
    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">',
             '  <!-- confusione mark (ring + swirl C). Original artwork, see docs/branding.md -->',
             '  ' + GRAD]
    if bg:
        parts.append(f'  <rect width="256" height="256" rx="56" fill="{bg}"/>')
    if ring and not small:
        parts.append(f'  <circle cx="128" cy="128" r="110" fill="none" stroke="{stroke}" stroke-width="16"/>')
    parts.append(f'  <path d="{c_path(r, tail_r, tail_end)}" fill="none" stroke="{stroke}" '
                 f'stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round"/>')
    parts.append(f'  <circle cx="128" cy="128" r="{dot_r}" fill="{dot}"/>')
    parts.append('</svg>\n')
    return '\n'.join(parts)

VARIANTS = {
    'confusione-mark': svg('url(#cg)', '#FFB547'),
    'confusione-mark-white': svg('#FFFFFF', '#FFFFFF'),
    'confusione-app-icon': svg('url(#cg)', '#FFB547', bg='#0B0A12'),
    'confusione-app-icon-small': svg('url(#cg)', '#FFB547', bg='#0B0A12', small=True),
}

def main():
    out = os.path.join(HERE, 'png')
    os.makedirs(out, exist_ok=True)
    for name, text in VARIANTS.items():
        with open(os.path.join(HERE, name + '.svg'), 'w') as f:
            f.write(text)
    def render(name, size, dest):
        cairosvg.svg2png(url=os.path.join(HERE, name + '.svg'), write_to=dest,
                         output_width=size, output_height=size)
    for s in (16, 32, 64, 128, 256):
        render('confusione-app-icon-small' if s <= 32 else 'confusione-app-icon', s,
               os.path.join(out, f'icon_{s}x{s}.png'))
    render('confusione-mark', 512, os.path.join(out, 'mark-512.png'))
    render('confusione-mark', 256, os.path.join(out, 'mark-256.png'))
    render('confusione-mark-white', 512, os.path.join(out, 'mark-white-512.png'))
    render('confusione-app-icon', 1024, os.path.join(out, 'osx-1024.png'))

if __name__ == '__main__':
    main()
