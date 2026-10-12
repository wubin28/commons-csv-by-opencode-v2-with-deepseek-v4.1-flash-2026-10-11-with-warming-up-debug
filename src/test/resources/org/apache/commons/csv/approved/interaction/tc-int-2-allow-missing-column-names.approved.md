# TC-INT-2 — allowMissingColumnNames accepts the blank column, the required column is present: pass

## Meta
table: INT
rule: interaction
case: TC-INT-2
variant: v1
source: 02-3 Step 7 TC-INT-2
adr: D3

## Input
requiredHeaders: id
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: true
skipHeaderRecord: (default)

## Data
```csv
id,
1,2
```

## Expected
outcome: success
headerMapSize: 2
recordCount: 1
record[0].get(id): 1
