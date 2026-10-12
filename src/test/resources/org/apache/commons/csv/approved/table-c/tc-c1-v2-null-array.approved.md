# TC-C1 v2 — setRequiredHeaders((String[]) null) disables the requirement

## Meta
table: C
rule: R-C1
case: TC-C1
variant: v2
source: 02-3 Step 7 TC-C1 (v2)
adr: D7a, D10

## Input
requiredHeaders: (null)
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
recordCount: 1
