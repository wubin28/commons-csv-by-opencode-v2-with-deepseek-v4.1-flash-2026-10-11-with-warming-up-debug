# Approved Scenarios — fixture DSL

Each `*.approved.md` file is **the single input** that the approval tests read and execute. It doubles as
the product-review material: scanning the fixture is enough to see input and expected outcome, no Java
assertion code required.

- Test entry: `org.apache.commons.csv.approved.RequiredHeadersApprovalTest` (one `@ParameterizedTest`
  per decision table, each invocation shown as an individual, debuggable node in the VSCode Testing panel).
- Parser: `ApprovalFixtureParser` reads the markdown structure only.
- Executor: `ApprovalScenarioExecutor` turns a parsed fixture into real API calls and assertions.

## File layout

```
approved/
  README.md            <- this file
  table-c/             <- build-time configuration shape (CSVFormat.validate)
  table-a/             <- parse-time pre-checks (CSVParser.createHeaders)
  table-b/             <- required-column existence
  interaction/         <- cross-feature regressions (TC-INT-*)
  config/              <- CSVFormat object contracts (TC-CFG-*)
```

File name: `<caseId>-<variant>-<slug>.approved.md`, e.g. `tc-a2-v1-no-header-one-required.approved.md`.
Display name in the Testing panel: `[<table>/<rule>] <case> <variant> - <title>`.

## Blocks (fixed H2 sections)

### `## Meta` (informational, used for the display name and traceability)

| key | meaning | example |
|---|---|---|
| `table` | decision table / group | `A` |
| `rule` | decision-table rule id | `R-A2` |
| `case` | acceptance case id | `TC-A2` |
| `variant` | variant id | `v1` |
| `source` | provenance | `02-3 Step 7 TC-A2 (v1)` |
| `adr` | governing ADR decisions | `D5, D6, D8` |

### `## Input` (`key: value`, builder configuration)

Keys: `requiredHeaders`, `header`, `ignoreHeaderCase`, `duplicateHeaderMode`,
`allowMissingColumnNames`, `skipHeaderRecord`, and the dialect keys `commentMarker`, `delimiter`,
`quote`, `trim`, `nullString` (used by the cross-feature regressions).

### `## Data`

A fenced ` ```csv ` block holds the raw CSV text. An **empty block** means an empty file.

### `## Expected`

- `outcome: success`, `outcome: failure` or `outcome: config`.
  `config` builds the format only (no parsing) and evaluates the format-object assertions below.
- On failure:
  - `phase: build` or `phase: parse` (default `parse`). `build` asserts the throw happens while building the
    format (`...builder()....get()`); `parse` asserts it happens when the parser is created
    (`CSVParser.parse(data, format)`), i.e. before the first record is read.
  - `throws: <fully-qualified exception class>`.
  - `message: <exact message>` (compared with **exact equality**).
- On success, these assertion lines appear as needed (absent lines are not checked):
  `formatRequiredHeaders:`, `headerNames:`, `headerMapSize:`, `headerIndex(<name>):`, `recordCount:`,
  `record[i].number:`, `record[i].isMapped(<name>):`, `record[i].get(<name>):`.
- Config-only assertions (`outcome: config`): `formatRequiredHeaders:`, `defensiveCopyHolds:`,
  `copyPreservesRequiredHeaders:`, `equalsDiffersOnRequiredHeaders:`.

## Placeholders

| placeholder | meaning | example |
|---|---|---|
| `(none)` | the setter is **never called** (null / unset) | `header: (none)`, `requiredHeaders: (none)` |
| `(empty)` | call the **no-arg / empty-array** form | `header: (empty)` → `setHeader()` (auto-detect); `requiredHeaders: (empty)` → `setRequiredHeaders()` |
| `(null)` | pass `null` explicitly | `requiredHeaders: (null)` → `setRequiredHeaders((String[]) null)` |
| `(default)` | do **not** set it; use the library default (defaults follow the source, not prose) | `ignoreHeaderCase: (default)` |
| `(blank)` | as a list element: a whitespace string (e.g. `" "`) | `requiredHeaders: currency, (blank), amount` |

List elements (`requiredHeaders`, `header`) are comma-separated. An element may be `(empty)` (the empty
string `""`), `(null)` (a `null` element), `(blank)` (a whitespace string), or a literal value.
Expected values in `record[i].get(<name>):` lines reuse `(empty)` / `(null)`.

## Header mode vs. the data block (avoids one extra record)

| mode | `## Input.header` | does `## Data` include the header row? |
|---|---|---|
| auto-detect | `(empty)` (calls `setHeader()`) | **yes** — first line is consumed as the header |
| explicit | `id, name` | **no** — data rows only, unless `skipHeaderRecord: true` |
| no header | `(none)` | data rows only |

## Notes

`Note:` / `## Notes` lines are **ignored by the parser** and exist only for reviewers, e.g. to flag where
a library default differs from the decision-table prose. The approved `02-2`/`02-3` documents are never edited.

## Regeneration (optional, local only)

`mvn test -Dapproved.regen=true` rewrites **only the `## Expected` block** of each fixture from the actual
result and passes. Review with `git diff`. `## Meta`/`## Input`/`## Data` are never touched.
