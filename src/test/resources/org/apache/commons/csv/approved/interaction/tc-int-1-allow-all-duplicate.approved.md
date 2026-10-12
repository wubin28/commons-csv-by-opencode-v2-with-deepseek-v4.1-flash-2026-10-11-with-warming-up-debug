# TC-INT-1 — ALLOW_ALL permits the duplicate, the required column is present: pass

## Meta
table: INT
rule: interaction
case: TC-INT-1
variant: v1
source: 02-3 Step 7 TC-INT-1
adr: D3

## Input
requiredHeaders: id
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: ALLOW_ALL
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,id
1,2
```

## Expected
outcome: success
headerMapSize: 1
recordCount: 1
record[0].get(id): 2
