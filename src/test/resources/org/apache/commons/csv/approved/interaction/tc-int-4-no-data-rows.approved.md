# TC-INT-4 — header only, no data rows: the column check still runs at construction

## Meta
table: INT
rule: interaction
case: TC-INT-4
variant: v1
source: 02-3 Step 7 TC-INT-4
adr: D5

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
```

## Expected
outcome: success
headerNames: id, name
recordCount: 0
