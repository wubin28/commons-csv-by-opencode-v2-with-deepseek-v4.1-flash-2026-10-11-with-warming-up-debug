# TC-B2e — auto-detect header but the file is empty: a zero-column header

## Meta
table: B
rule: R-B2
case: TC-B2e
variant: v1
source: 02-3 Step 7 TC-B2e (ADR D9 variant)
adr: D9, D8

## Input
requiredHeaders: currency
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency] in []
