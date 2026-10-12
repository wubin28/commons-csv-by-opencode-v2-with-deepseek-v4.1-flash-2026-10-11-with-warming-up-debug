# TC-B5b — one case-only-difference column and one fully missing column, ignoreHeaderCase=false

## Meta
table: B
rule: R-B5
case: TC-B5b
variant: v1
source: 02-3 Step 7 TC-B5b
adr: D2, D8

## Input
requiredHeaders: currency, amount
header: (empty)
ignoreHeaderCase: false
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
Currency,id
100,1
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency, amount] in [Currency, id]
