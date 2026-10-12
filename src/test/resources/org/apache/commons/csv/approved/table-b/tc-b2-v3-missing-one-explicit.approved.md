# TC-B2 v3 — one required column is completely absent (explicit header)

## Meta
table: B
rule: R-B2
case: TC-B2
variant: v3
source: 02-3 Step 7 TC-B2 (v3)
adr: D2, D8, D9

## Input
requiredHeaders: id, currency
header: id, name
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: true

## Data
```csv
1,2
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency] in [id, name]
