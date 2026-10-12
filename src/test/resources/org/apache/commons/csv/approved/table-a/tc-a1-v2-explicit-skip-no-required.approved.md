# TC-A1 v2 — no required headers, explicit header + skipHeaderRecord: behaviour unchanged

## Meta
table: A
rule: R-A1
case: TC-A1
variant: v2
source: 02-3 Step 7 TC-A1 (v2)
adr: D5, D7

## Input
requiredHeaders: (none)
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
