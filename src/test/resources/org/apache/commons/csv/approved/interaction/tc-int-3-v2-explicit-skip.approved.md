# TC-INT-3 v2 — explicit header with skipHeaderRecord: the header row is consumed

## Meta
table: INT
rule: interaction
case: TC-INT-3
variant: v2
source: 02-3 Step 7 TC-INT-3 (v2)
adr: D5

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
record[0].get(id): 1
