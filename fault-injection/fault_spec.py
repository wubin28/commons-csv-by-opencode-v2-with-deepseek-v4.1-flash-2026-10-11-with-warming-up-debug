#!/usr/bin/env python3
"""Single source of truth for the fault-injection exercise (scheme 2).

Every entry injects ONE semantics-corresponding fault into production code at a
specific span, expressed as an exact ``anchor`` -> ``replacement`` text pair.

Fields
------
id          fault id (also the patch file name)
title       one-line human title
file        path (relative to the commons-csv repo root) of the production file
phase       "build" | "parse"  (where the protected behaviour lives)
behavior    what production behaviour this fault is meant to break
anchor      exact *before* source text (must occur exactly once in ``file``)
replacement exact *after* source text
red         list of (fixture filename, expected-fragment) pairs.  ``fixture`` is
            the bare ``*.approved.md`` file name (that is what the executor puts
            into its assertion message).  ``fragment`` must be a substring of the
            failure text recorded by surefire when this fault is injected.
note        free-form explanation rendered into the hand guide

NOTE on anchors: they are written flush-left with the same indentation as the
Java source, so that ``str.replace`` is an exact, whitespace-sensitive match.
"""

FAULTS = [
    dict(
        id="FP-01",
        title="构建期 required 元素校验：消息被改坏（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVFormat.java",
        phase="build",
        behavior="CSVFormat.validate() 对 requiredHeaders 中的 null/空白元素抛 IllegalArgumentException。",
        anchor='throw new IllegalArgumentException("A required name is missing in " + Arrays.toString(requiredHeaders));',
        replacement='throw new IllegalArgumentException("A required name is absent in " + Arrays.toString(requiredHeaders));',
        red=[
            ("tc-c3-v1-blank-element.approved.md", "A required name is absent in [currency, , amount]"),
            ("tc-c3-v2-null-element.approved.md", "A required name is absent in [currency, null, amount]"),
        ],
        note="只改坏消息文案、不让异常消失：代码仍在构建期抛出，测试以 message mismatch 变红，"
             "证明 R-C3 的用例确实钉住了这条消息与它出现的位置（phase=build）。",
    ),
    dict(
        id="FP-02",
        title="构建期重复表头：消息被改坏（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVFormat.java",
        phase="build",
        behavior="CSVFormat.validate() 拒绝显式表头里的重复列名。",
        anchor='"The header contains a duplicate name: \\"%s\\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().", header,',
        replacement='"The header has a duplicate name: \\"%s\\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().", header,',
        red=[
            ("tc-a3c-explicit-duplicate-build.approved.md", 'The header has a duplicate name: "id" in [id, id]'),
        ],
        note="只破坏 CSVFormat 里的那条文案（CSVParser 里的同名文案不动），因此精确只打红构建期的 TC-A3c。",
    ),
    dict(
        id="FP-03",
        title="解析期空列名：消息被改坏（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="CSVParser.createHeaders() 在 allowMissingColumnNames=false 时拒绝空列名。",
        anchor='throw new IllegalArgumentException("A header name is missing in " + Arrays.toString(headerRecord));',
        replacement='throw new IllegalArgumentException("A header name is absent in " + Arrays.toString(headerRecord));',
        red=[
            ("tc-a3a-auto-blank-header.approved.md", "A header name is absent in [id, , name]"),
        ],
        note="该文案在 CSVParser 中唯一，改动只影响解析期空列名分支，精确打红 TC-A3a。",
    ),
    dict(
        id="FP-04",
        title="解析期重复表头：消息被改坏（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="CSVParser.createHeaders() 按 DuplicateHeaderMode 拒绝重复列名。",
        anchor='"The header contains a duplicate name: \\"%s\\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().",',
        replacement='"The header has a duplicate name: \\"%s\\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().",',
        red=[
            ("tc-a3b-auto-duplicate-header.approved.md", 'The header has a duplicate name: "id" in [id, id]'),
        ],
        note="FP-02 打红构建期（TC-A3c），本故障打红解析期（TC-A3b）——同一条消息、两个不同抛出点，正好证明两个用例各自守在正确阶段。",
    ),
    dict(
        id="FP-05",
        title="无表头错误：消息被改坏（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="配置了 requiredHeaders 但格式无表头时，validateRequiredHeaders() 抛「无法校验」错。",
        anchor='throw new IllegalArgumentException("Required name(s) " + required + " cannot be verified because the format has no header");',
        replacement='throw new IllegalArgumentException("Missing name(s) " + required + " cannot be verified because the format has no header");',
        red=[
            ("tc-a2-v1-no-header-one-required.approved.md", "Missing name(s) [currency] cannot be verified because the format has no header"),
            ("tc-a2-v2-no-header-two-required.approved.md", "Missing name(s) [currency, amount] cannot be verified because the format has no header"),
        ],
        note="只打红两条「无表头」用例，不波及任何「缺列」用例——证明 R-A2 与 R-B2 分属两条不同分支。",
    ),
    dict(
        id="FP-06",
        title="去掉 required 名的声明顺序去重",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="validateRequiredHeaders() 按声明顺序对 requiredHeaders 去重，重复名只报一次。",
        anchor='''        for (final String requiredHeader : format.getRequiredHeaders()) {
            if (!required.contains(requiredHeader)) {
                required.add(requiredHeader);
            }
        }''',
        replacement='''        for (final String requiredHeader : format.getRequiredHeaders()) {
            required.add(requiredHeader);
        }''',
        red=[
            ("tc-c3-x1-duplicate-element.approved.md", "[currency, currency] in [id, name]"),
        ],
        note="只有 TC-C3 x1 使用了重复的 required 名（currency, currency），因此精确只打红它；"
             "其余用例的必需列名不重复，去重与否结果相同，故保持绿。",
    ),
    dict(
        id="FP-07",
        title="缺列消息列出全部 required，而不是仅缺失者（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="「缺列」消息只列出真正缺失的必需列，而不是列出全部必需列。",
        anchor='"The header is missing required name(s): " + missing + " in "',
        replacement='"The header is missing required name(s): " + required + " in "',
        red=[
            ("tc-b2-v1-missing-one-auto.approved.md", "[id, currency] in [id, name]"),
            ("tc-b2-v3-missing-one-explicit.approved.md", "[id, currency] in [id, name]"),
            ("tc-b5a-both-ignore-true.approved.md", "[currency, amount] in [Currency, id]"),
        ],
        note="只改「要报哪些列」（missing → required），不删除抛出：因此只有「部分缺失」的用例报错变化"
             "（B2 v1/B2 v3/B5a 变红），而「全缺」用例（B2 v2/B5b）与「缺一列且即全体」用例（B4/B2e 等）"
             "消息不变、保持绿；success 型用例根本不抛错、也不受影响。",
    ),
    dict(
        id="FP-07c",
        title="缺列集合在第一个缺失列后就停止收集（仍抛出）",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="所有真正缺失的必需列都要被报告，而不是只报告第一个。",
        anchor='''        final List<String> missing = new ArrayList<>();
        for (final String name : required) {
            if (!headerMap.containsKey(name)) {
                missing.add(name);
            }
        }''',
        replacement='''        final List<String> missing = new ArrayList<>();
        for (final String name : required) {
            if (!headerMap.containsKey(name)) {
                missing.add(name);
                break;
            }
        }''',
        red=[
            ("tc-b2-v2-missing-all-auto.approved.md", "The header is missing required name(s): [currency] in [id, name]"),
            ("tc-b5b-both-ignore-false.approved.md", "The header is missing required name(s): [currency] in [Currency, id]"),
        ],
        note="与 FP-07 互补：这里只影响「缺两列」的用例（B2 v2 / B5b），「缺一列」的用例保持绿。",
    ),
    dict(
        id="FP-08a",
        title="ignoreHeaderCase 失效：表头映射恒为大小写敏感",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="ignoreHeaderCase=true 时用大小写不敏感的表头映射做必需列匹配。",
        anchor='''    private Map<String, Integer> createEmptyHeaderMap() {
        return format.getIgnoreHeaderCase() ?
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER) :
                new LinkedHashMap<>();
    }''',
        replacement='''    private Map<String, Integer> createEmptyHeaderMap() {
        return new LinkedHashMap<>();
    }''',
        red=[
            ("tc-b5a-both-ignore-true.approved.md", "[currency, amount] in [Currency, id]"),
            ("tc-b3-case-diff-ignore-true.approved.md", "outcome mismatch in tc-b3-case-diff-ignore-true.approved.md"),
            ("tc-b3-x3-case-diff-explicit.approved.md", "outcome mismatch in tc-b3-x3-case-diff-explicit.approved.md"),
        ],
        note="主目标是 TC-B5a；另两条是本应为 success 的 TC-B3 变体被连带打红（这正是「预期变红集合」"
             "必须覆盖 success 型用例的原因：fault 破坏了大小写匹配，它们再也读不出 currency）。",
    ),
    dict(
        id="FP-08b",
        title="ignoreHeaderCase 失效：表头映射恒为大小写不敏感",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="ignoreHeaderCase=false 时必须大小写敏感地匹配（Currency 不等于 currency）。",
        anchor='''    private Map<String, Integer> createEmptyHeaderMap() {
        return format.getIgnoreHeaderCase() ?
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER) :
                new LinkedHashMap<>();
    }''',
        replacement='''    private Map<String, Integer> createEmptyHeaderMap() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }''',
        red=[
            ("tc-b4-case-diff-ignore-false.approved.md", "outcome mismatch in tc-b4-case-diff-ignore-false.approved.md"),
            ("tc-b5b-both-ignore-false.approved.md", "[amount] in [Currency, id]"),
        ],
        note="与 FP-08a 方向相反：这里破坏了「大小写敏感」这一侧（ignoreHeaderCase=false）。TC-B4 的设防是"
             "「Currency 不该匹配 currency」，被放宽匹配后不再抛错 → outcome mismatch；TC-B5b 被放宽后只报 amount。",
    ),
    dict(
        id="FP-09",
        title="缺列错误回显的 actual 在表头为空时退化为 null",
        file="src/main/java/org/apache/commons/csv/CSVParser.java",
        phase="parse",
        behavior="当表头无法读出（headerRecord==null）时，错误消息的 actual 回显为空数组 []，而非 null。",
        anchor='                    + Arrays.toString(headerRecord == null ? Constants.EMPTY_STRING_ARRAY : headerRecord));',
        replacement='                    + Arrays.toString(headerRecord));',
        red=[
            ("tc-b2e-auto-empty-file.approved.md", "[currency] in null"),
            ("tc-int-5-comment-only-file.approved.md", "[id] in null"),
        ],
        note="Arrays.toString(null) 返回字面量 null，因此消息从「… in []」变成「… in null」。"
             "只影响「零列表头」的两条用例（空文件、纯注释文件），其余缺列用例 headerRecord 非 null，保持绿。",
    ),
]
