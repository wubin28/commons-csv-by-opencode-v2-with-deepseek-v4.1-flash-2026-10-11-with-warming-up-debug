# TC-B2 v2 — every required column is completely absent (auto header)

## Meta
table: B
rule: R-B2
case: TC-B2
variant: v2
source: 02-3 Step 7 TC-B2 (v2)
adr: D2, D8, D9

## Input
requiredHeaders: currency, amount
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,name
1,2
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency, amount] in [id, name]
