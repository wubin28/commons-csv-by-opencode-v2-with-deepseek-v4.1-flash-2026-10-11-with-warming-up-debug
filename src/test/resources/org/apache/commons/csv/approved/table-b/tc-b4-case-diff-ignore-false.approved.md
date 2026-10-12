# TC-B4 — the required column differs only by case and ignoreHeaderCase=false: fail

## Meta
table: B
rule: R-B4
case: TC-B4
variant: v1
source: 02-3 Step 7 TC-B4
adr: D2, D8

## Input
requiredHeaders: currency
header: (empty)
ignoreHeaderCase: false
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
Currency
100
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency] in [Currency]
