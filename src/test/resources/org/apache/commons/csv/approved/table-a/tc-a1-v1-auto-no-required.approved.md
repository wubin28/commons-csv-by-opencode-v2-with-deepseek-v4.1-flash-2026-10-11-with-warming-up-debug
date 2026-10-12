# TC-A1 v1 — no required headers, auto-detected header: behaviour unchanged

## Meta
table: A
rule: R-A1
case: TC-A1
variant: v1
source: 02-3 Step 7 TC-A1 (v1)
adr: D5, D7

## Input
requiredHeaders: (none)
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
outcome: success
headerNames: id, name
recordCount: 1
