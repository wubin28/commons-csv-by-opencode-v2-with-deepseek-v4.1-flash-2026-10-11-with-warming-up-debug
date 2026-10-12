# TC-A3a — a blank header column fails before the missing-column check

## Meta
table: A
rule: R-A3
case: TC-A3a
variant: v1
source: 02-3 Step 7 TC-A3a
adr: D3, D5, D8

## Input
requiredHeaders: currency
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,,name
1,2,3
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: A header name is missing in [id, , name]
