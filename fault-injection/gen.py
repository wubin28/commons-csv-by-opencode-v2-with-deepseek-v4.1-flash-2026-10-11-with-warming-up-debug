#!/usr/bin/env python3
"""Generate scheme-2 artifacts from the single source of truth.

    python3 gen.py

Produces:
  faults/patches/<id>.patch      one unified-diff per fault (git-appliable)
  faults/ids.txt                 ordered fault ids
  FAULT-INJECTION-GUIDE.md       human-readable hand guide

If ``out/actuals/<id>.actual.json`` exists (written by ``run-fault-injection.sh``),
the guide renders the *observed* failure text; otherwise it renders the declared
fragment only.  Run this again after a run to embed the observed messages.
"""
import glob
import json
import os
import subprocess
import sys

from fault_spec import FAULTS

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
PATCH_DIR = os.path.join(HERE, "faults", "patches")
ACTUALS_DIR = os.path.join(HERE, "out", "actuals")
FIXTURE_ROOT = os.path.join(REPO, "src", "test", "resources", "org", "apache", "commons", "csv", "approved")


def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], capture_output=True, text=True)


def ensure_clean():
    if git("diff", "--quiet").returncode or git("diff", "--cached", "--quiet").returncode:
        sys.exit("ABORT: working tree not clean; commit or stash first.")


def fixture_outcome(fixture):
    matches = glob.glob(os.path.join(FIXTURE_ROOT, "*", fixture))
    if not matches:
        return None
    for line in open(matches[0], encoding="utf-8"):
        if line.startswith("outcome:"):
            return line.split(":", 1)[1].strip()
    return None


def failure_fixtures():
    names = [os.path.basename(p) for p in glob.glob(os.path.join(FIXTURE_ROOT, "*", "*.approved.md"))]
    return sorted(n for n in names if fixture_outcome(n) == "failure")


def fixture_expected_message(fixture):
    matches = glob.glob(os.path.join(FIXTURE_ROOT, "*", fixture))
    if not matches:
        return None
    for line in open(matches[0], encoding="utf-8"):
        if line.startswith("message:"):
            return line[len("message:"):].strip()
    return None


def gen_patches():
    ensure_clean()
    os.makedirs(PATCH_DIR, exist_ok=True)
    info = []
    for f in FAULTS:
        src_path = os.path.join(REPO, f["file"])
        src = open(src_path, encoding="utf-8").read()
        n = src.count(f["anchor"])
        if n != 1:
            sys.exit(f"ABORT {f['id']}: anchor occurs {n} time(s) in {f['file']}")
        line = src[:src.index(f["anchor"])].count("\n") + 1
        open(src_path, "w", encoding="utf-8").write(src.replace(f["anchor"], f["replacement"], 1))
        try:
            diff = git("diff", "--no-color", "--", f["file"])
        finally:
            git("checkout", "--", f["file"])
        if diff.returncode != 0:
            sys.exit(f"ABORT {f['id']}: git diff failed: {diff.stderr}")
        open(os.path.join(PATCH_DIR, f["id"] + ".patch"), "w", encoding="utf-8").write(diff.stdout)
        check = git("apply", "--check", os.path.join(PATCH_DIR, f["id"] + ".patch"))
        if check.returncode != 0:
            sys.exit(f"ABORT {f['id']}: generated patch does not apply: {check.stderr}")
        info.append(dict(f, line=line, cls=os.path.basename(f["file"]), patch_lines=len(diff.stdout.splitlines())))
        print(f"[gen] {f['id']}: {info[-1]['cls']}:{line}  ->  faults/patches/{f['id']}.patch")
    open(os.path.join(HERE, "faults", "ids.txt"), "w").write("\n".join(f["id"] for f in FAULTS) + "\n")
    return info


def actuals_for(fault_id):
    path = os.path.join(ACTUALS_DIR, fault_id + ".actual.json")
    if os.path.exists(path):
        return json.load(open(path, encoding="utf-8"))["messages"]
    return {}


def render_guide(info):
    out = []
    out.append("# 手工故障注入指南（方案二：unified-diff 补丁 + `git apply` / `git apply -R`）")
    out.append("")
    out.append("> 本文件由 `fault-injection/gen.py` 从 `fault-injection/fault_spec.py`（单一事实源）自动生成，请勿手改。")
    out.append("> 每条故障对应一份 `fault-injection/faults/patches/<id>.patch`；这里给出人类可读的"
               "类名+行号、注入前/后代码、以及注入后运行测试时期望看到的报错。")
    out.append("")
    out.append("## 用法（VSCode 手工单步）")
    out.append("")
    out.append("1. 在 `commons-csv/` 目录打开 VSCode，按 `03-3` 指南把断点下到对应生产代码行。")
    out.append("2. 按本指南「注入后」片段手工修改该行，保存（不必管补丁文件）。")
    out.append("3. 在 Testing 面板单独 Debug 目标用例（或其所在分组方法），观察本指南「期望看到」的报错。")
    out.append("4. 观察完用 `git checkout -- <file>` 还原；或直接 `git apply -R faults/patches/<id>.patch`。")
    out.append("")
    out.append("## 一览")
    out.append("")
    out.append("| 故障 | 生产代码 | 阶段 | 预期变红集合（精确） |")
    out.append("|---|---|---|---|")
    for f in info:
        red = "、".join(fx for fx, _ in f["red"])
        out.append(f"| `{f['id']}` | `{f['cls']}:{f['line']}` | {f['phase']} | {red} |")
    out.append("")
    out.append("## 覆盖矩阵（每条 failure 用例 → 打红它的故障）")
    out.append("")
    out.append("| failure 用例（16 条） | 打红它的故障（`主目标` 用加粗，连带用常规） |")
    out.append("|---|---|")
    uncovered = []
    for fx in failure_fixtures():
        ids = [f["id"] for f in info if fx in [r[0] for r in f["red"]]]
        if not ids:
            uncovered.append(fx)
        rendered = "、".join(f"`{i}`" for i in ids) if ids else "**未被任何故障覆盖！**"
        out.append(f"| `{fx}` | {rendered} |")
    out.append("")
    out.append("> 另：以下 **success / config 型** 用例会被某些故障连带打红（它们没有异常消息，"
               "因此只计入「预期变红集合」、不预置片段）：")
    failset = set(failure_fixtures())
    collateral = sorted({fx for f in info for fx, _ in f["red"] if fx not in failset})
    for fx in collateral:
        ids = [f["id"] for f in info if fx in [r[0] for r in f["red"]]]
        out.append(f"> - `{fx}` ← {('、'.join('`'+i+'`' for i in ids))}")
    out.append("")
    if uncovered:
        print("[gen] WARNING: failure fixtures not covered by any fault:", uncovered)
    for f in info:
        actuals = actuals_for(f["id"])
        out.append(f"## {f['id']} — {f['title']}")
        out.append("")
        out.append(f"- **生产代码**：`{f['cls']}:{f['line']}`（`{f['file']}`）")
        out.append(f"- **阶段**：{f['phase']}")
        out.append(f"- **被保护的行为**：{f['behavior']}")
        out.append("- **注入前**：")
        out.append("")
        out.append("```java")
        out.append(f["anchor"])
        out.append("```")
        out.append("")
        out.append("- **注入后**：")
        out.append("")
        out.append("```java")
        out.append(f["replacement"])
        out.append("```")
        out.append("")
        out.append(f"- **预期变红集合（精确，共 {len(f['red'])} 条）**：")
        for fx, frag in f["red"]:
            expected = fixture_expected_message(fx)
            out.append(f"  - `{fx}`")
            if expected:
                out.append(f"    - 本应看到：`{expected}`")
            observed = actuals.get(fx)
            if observed:
                first = observed.splitlines()[0]
                out.append(f"    - 注入后实际看到：`{first}`")
            else:
                out.append(f"    - 注入后必须包含片段：`{frag}`")
        out.append("")
        out.append(f"- **说明**：{f['note']}")
        out.append("")
    open(os.path.join(HERE, "FAULT-INJECTION-GUIDE.md"), "w", encoding="utf-8").write("\n".join(out) + "\n")
    print("[gen] FAULT-INJECTION-GUIDE.md written")


if __name__ == "__main__":
    render_guide(gen_patches())
