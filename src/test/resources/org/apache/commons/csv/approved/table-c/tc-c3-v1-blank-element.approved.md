# TC-C3 v1 — a blank required name is rejected while building the format

## Meta
table: C
rule: R-C3
case: TC-C3
variant: v1
source: 02-3 Step 7 TC-C3 (v1)
adr: D7c, D8

## Input
requiredHeaders: currency, (empty), amount
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
phase: build
throws: java.lang.IllegalArgumentException
message: A required name is missing in [currency, , amount]
