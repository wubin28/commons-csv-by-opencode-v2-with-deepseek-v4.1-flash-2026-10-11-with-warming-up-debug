# TC-B1 v4 — every required column present with ignoreHeaderCase=false

## Meta
table: B
rule: R-B1
case: TC-B1
variant: v4
source: 02-3 Step 7 TC-B1 (v4)
adr: D2, D5

## Input
requiredHeaders: id, name
header: (empty)
ignoreHeaderCase: false
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
record[0].get(name): 2
