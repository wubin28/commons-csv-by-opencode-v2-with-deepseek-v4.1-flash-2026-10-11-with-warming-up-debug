# TC-INT-3 v1 — explicit header without skipHeaderRecord: the data block holds only data rows

## Meta
table: INT
rule: interaction
case: TC-INT-3
variant: v1
source: 02-3 Step 7 TC-INT-3 (v1)
adr: D5

## Input
requiredHeaders: id, name
header: id, name
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
1,2
```

## Expected
outcome: success
headerNames: id, name
recordCount: 1
record[0].get(id): 1
