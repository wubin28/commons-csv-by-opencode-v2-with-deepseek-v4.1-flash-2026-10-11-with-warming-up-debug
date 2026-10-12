# TC-INT-3 v3 — auto-detected header: same required-column verdict as the explicit modes

## Meta
table: INT
rule: interaction
case: TC-INT-3
variant: v3
source: 02-3 Step 7 TC-INT-3 (v3)
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
1,2
```

## Expected
outcome: success
headerNames: id, name
recordCount: 1
