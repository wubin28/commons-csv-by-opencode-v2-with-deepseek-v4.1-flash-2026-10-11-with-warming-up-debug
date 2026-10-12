# TC-B1 v2 — every required column present (explicit header + skipHeaderRecord)

## Meta
table: B
rule: R-B1
case: TC-B1
variant: v2
source: 02-3 Step 7 TC-B1 (v2)
adr: D2, D5

## Input
requiredHeaders: id, name
header: id, name
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: true

## Data
```csv
id,name
1,2
```

## Expected
outcome: success
headerNames: id, name
recordCount: 1
record[0].number: 1
record[0].get(id): 1
