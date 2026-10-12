# TC-C1 v3 — setRequiredHeaders() (empty array) means no requirement

## Meta
table: C
rule: R-C1
case: TC-C1
variant: v3
source: 02-3 Step 7 TC-C1 (v3)
adr: D7b, D10

## Input
requiredHeaders: (empty)
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
