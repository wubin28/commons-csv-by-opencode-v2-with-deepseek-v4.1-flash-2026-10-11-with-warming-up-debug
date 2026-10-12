# TC-A2 v1 — required headers set, but the format has no header

## Meta
table: A
rule: R-A2
case: TC-A2
variant: v1
source: 02-3 Step 7 TC-A2 (v1)
adr: D5, D6, D8

## Input
requiredHeaders: currency
header: (none)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
1,2
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: Required name(s) [currency] cannot be verified because the format has no header
