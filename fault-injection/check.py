#!/usr/bin/env python3
"""Assert one injected fault's surefire report against fault_spec.

    python3 check.py <fault-id> <surefire-xml>

Asserts:
  * the set of red fixtures equals the fault's declared ``red`` set (EXACTLY), and
  * every red fixture's failure text contains its declared ``fragment``.

Also dumps ``out/actuals/<id>.actual.json`` so the guide can embed observed text.
Exit code 0 = pass, 1 = fail.
"""
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

from fault_spec import FAULTS

HERE = os.path.dirname(os.path.abspath(__file__))
FIXTURE_RE = re.compile(r"([A-Za-z0-9._-]+\.approved\.md)")


def main():
    fault_id, report_path = sys.argv[1], sys.argv[2]
    fault = next(f for f in FAULTS if f["id"] == fault_id)
    actual = {}
    for testcase in ET.parse(report_path).getroot().iter("testcase"):
        for child in list(testcase):
            if child.tag in ("failure", "error"):
                text = (child.get("message") or "") + "\n" + (child.text or "")
                match = FIXTURE_RE.search(text)
                if match:
                    actual[match.group(1)] = text

    expected = {fx: frag for fx, frag in fault["red"]}
    ok = True
    if set(expected) != set(actual):
        ok = False
        print(f"[{fault_id}] RED-SET MISMATCH")
        print(f"    expected: {sorted(expected)}")
        print(f"    actual  : {sorted(actual)}")
        if set(expected) - set(actual):
            print(f"    missing : {sorted(set(expected) - set(actual))}")
        if set(actual) - set(expected):
            print(f"    extra   : {sorted(set(actual) - set(expected))}")
    else:
        print(f"[{fault_id}] red set OK ({len(expected)}): {', '.join(sorted(expected))}")

    for fx, frag in fault["red"]:
        text = actual.get(fx, "")
        if frag not in text:
            ok = False
            got = text.splitlines()[0][:240] if text else "(no failure text)"
            print(f"[{fault_id}] FRAGMENT MISSING in {fx}\n    want: {frag}\n    got : {got}")
        else:
            print(f"[{fault_id}] fragment OK in {fx}")

    out_dir = os.path.join(HERE, "out", "actuals")
    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, fault_id + ".actual.json"), "w", encoding="utf-8") as fh:
        json.dump({"actual_red": sorted(actual), "messages": actual}, fh, ensure_ascii=False, indent=2)
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
