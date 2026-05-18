"""Extract plain text from .docx / .pptx / .xlsx using zipfile + XML only (no external deps)."""
import sys
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

NS_W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
NS_A = "{http://schemas.openxmlformats.org/drawingml/2006/main}"
NS_P = "{http://schemas.openxmlformats.org/presentationml/2006/main}"
NS_S = "{http://schemas.openxmlformats.org/spreadsheetml/2006/main}"


def extract_docx(path: Path) -> str:
    out = []
    with zipfile.ZipFile(path) as z:
        with z.open("word/document.xml") as f:
            tree = ET.parse(f)
        for p in tree.iter(NS_W + "p"):
            line = []
            for t in p.iter(NS_W + "t"):
                if t.text:
                    line.append(t.text)
            out.append("".join(line))
    return "\n".join(out)


def extract_pptx(path: Path) -> str:
    out = []
    with zipfile.ZipFile(path) as z:
        slides = sorted([n for n in z.namelist() if n.startswith("ppt/slides/slide") and n.endswith(".xml")],
                        key=lambda x: int("".join(filter(str.isdigit, x.rsplit('/', 1)[-1]))))
        for s in slides:
            out.append(f"\n===== {s} =====")
            with z.open(s) as f:
                tree = ET.parse(f)
            for p in tree.iter(NS_A + "p"):
                line = []
                for t in p.iter(NS_A + "t"):
                    if t.text:
                        line.append(t.text)
                if line:
                    out.append("".join(line))
    return "\n".join(out)


def extract_xlsx(path: Path) -> str:
    out = []
    with zipfile.ZipFile(path) as z:
        shared = []
        if "xl/sharedStrings.xml" in z.namelist():
            with z.open("xl/sharedStrings.xml") as f:
                tree = ET.parse(f)
            for si in tree.iter(NS_S + "si"):
                parts = []
                for t in si.iter(NS_S + "t"):
                    if t.text:
                        parts.append(t.text)
                shared.append("".join(parts))

        # workbook sheet list
        with z.open("xl/workbook.xml") as f:
            wb = ET.parse(f).getroot()
        rels_ns = "{http://schemas.openxmlformats.org/officeDocument/2006/relationships}"
        sheet_meta = []
        for s in wb.iter(NS_S + "sheet"):
            sheet_meta.append((s.attrib.get("name"), s.attrib.get(rels_ns + "id")))

        with z.open("xl/_rels/workbook.xml.rels") as f:
            rels_tree = ET.parse(f).getroot()
        rels_map = {}
        for r in rels_tree:
            rels_map[r.attrib["Id"]] = r.attrib["Target"]

        for name, rid in sheet_meta:
            target = rels_map.get(rid, "")
            sheet_path = "xl/" + target if not target.startswith("xl/") else target
            if sheet_path not in z.namelist():
                continue
            out.append(f"\n===== Sheet: {name} =====")
            with z.open(sheet_path) as f:
                tree = ET.parse(f)
            for row in tree.iter(NS_S + "row"):
                cells = []
                for c in row.iter(NS_S + "c"):
                    t = c.attrib.get("t")
                    v = c.find(NS_S + "v")
                    if v is None:
                        is_node = c.find(NS_S + "is")
                        if is_node is not None:
                            text = "".join((tt.text or "") for tt in is_node.iter(NS_S + "t"))
                            cells.append(text)
                        else:
                            cells.append("")
                        continue
                    if t == "s":
                        idx = int(v.text)
                        cells.append(shared[idx] if idx < len(shared) else "")
                    else:
                        cells.append(v.text or "")
                out.append("\t".join(cells))
    return "\n".join(out)


def main():
    base = Path(sys.argv[1])
    out_dir = Path(sys.argv[2])
    out_dir.mkdir(parents=True, exist_ok=True)
    for fp in base.iterdir():
        if fp.is_dir():
            continue
        try:
            if fp.suffix.lower() == ".docx":
                text = extract_docx(fp)
            elif fp.suffix.lower() == ".pptx":
                text = extract_pptx(fp)
            elif fp.suffix.lower() == ".xlsx":
                text = extract_xlsx(fp)
            else:
                continue
            (out_dir / (fp.stem + ".txt")).write_text(text, encoding="utf-8")
            print(f"OK: {fp.name} -> {fp.stem}.txt ({len(text)} chars)")
        except Exception as e:
            print(f"FAIL: {fp.name}: {e}")


if __name__ == "__main__":
    main()
