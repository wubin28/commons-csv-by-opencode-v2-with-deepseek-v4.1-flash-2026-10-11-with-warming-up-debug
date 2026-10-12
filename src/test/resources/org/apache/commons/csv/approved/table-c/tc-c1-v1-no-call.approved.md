# TC-C1 v1 — setRequiredHeaders is never called

## Meta
table: C
rule: R-C1
case: TC-C1
variant: v1
source: 02-3 Step 7 TC-C1 (v1)
adr: D7a, D7b, D10

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
formatRequiredHeaders: (empty)
headerNames: id, name
headerMapSize: 2
recordCount: 1
