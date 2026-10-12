# fault-injection/ — 方案二（统一 diff 补丁 + `git apply` / `git apply -R`）

一次性、学习用途的故障注入设施：对 `commons-csv` 的 Approved Scenarios 验收测试
（`org.apache.commons.csv.approved.RequiredHeadersApprovalTest`）注入"与保护意图语义对应"的
生产代码故障，验证 `注入 → 变红 → 报错对应 → 撤销 → 变绿` 闭环。

它**不是**长期测试、**不进** CI、**不改** `pom.xml`。

## 布局

| 路径 | 作用 |
|---|---|
| `fault_spec.py` | **单一事实源**：每条故障的 `(file, anchor, replacement, 预期变红集合, 期望片段, 说明)` |
| `gen.py` | 校验锚点唯一性 → 由 `fault_spec.py` 生成补丁与手工指南 |
| `check.py` | 解析 surefire XML，断言"变红集合精确相等 + 报错含期望片段" |
| `run-fault-injection.sh` | 驱动：preflight → 逐条 注入/跑/断言/撤销/复绿（含 Ctrl-C 复原） |
| `faults/patches/<id>.patch` | 生成物：每故障一份 unified diff |
| `faults/ids.txt` | 生成物：故障执行顺序 |
| `FAULT-INJECTION-GUIDE.md` | 生成物：手工单步注入指南（类名+行号、注入前/后、期望报错） |
| `out/` | 运行产物：日志、报告摘要、实测报错 JSON（可删） |

## 用法

```bash
cd commons-csv/fault-injection

# 1) 生成补丁 + 指南（要求 commons-csv 工作区干净）
python3 gen.py

# 2) 跑完整闭环（注入→变红→断言→撤销→复绿），约几分钟
./run-fault-injection.sh

# 3) 把实测报错嵌入指南（可选）
python3 gen.py
```

手工单步：按 `FAULT-INJECTION-GUIDE.md` 在 VSCode 里手改对应行 → Debug 目标用例 → 观察报错 →
`git checkout -- <file>` 还原。

## 安全

- 注入只发生在 `git apply` 期间；脚本 `trap` 保证 Ctrl-C / TERM / 正常退出都会复原源码。
- 若某故障注入后测试仍为绿，`check.py` 会以"变红集合不匹配 / 片段缺失"如实报错，不会被悄悄放过。
