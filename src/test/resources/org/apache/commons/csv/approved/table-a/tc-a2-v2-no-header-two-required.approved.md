# TC-A2 v2 — two required headers, but the format has no header

## Meta
table: A
rule: R-A2
case: TC-A2
variant: v2
source: 02-3 Step 7 TC-A2 (v2)
adr: D5, D6, D8

## Input
requiredHeaders: currency, amount
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
message: Required name(s) [currency, amount] cannot be verified because the format has no header
