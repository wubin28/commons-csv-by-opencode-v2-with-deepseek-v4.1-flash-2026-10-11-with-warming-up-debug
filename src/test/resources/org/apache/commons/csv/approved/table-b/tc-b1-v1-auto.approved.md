# TC-B1 v1 — every required column is present (auto-detected header)

## Meta
table: B
rule: R-B1
case: TC-B1
variant: v1
source: 02-3 Step 7 TC-B1 (v1)
adr: D2, D5

## Input
requiredHeaders: id, name
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
headerMapSize: 2
recordCount: 1
record[0].number: 1
record[0].isMapped(id): true
record[0].get(id): 1
record[0].isMapped(name): true
record[0].get(name): 2
