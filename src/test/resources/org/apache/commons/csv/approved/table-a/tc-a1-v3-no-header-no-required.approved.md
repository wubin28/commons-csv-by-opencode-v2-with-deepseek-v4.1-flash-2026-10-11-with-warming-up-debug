# TC-A1 v3 — no required headers, no header at all: behaviour unchanged

## Meta
table: A
rule: R-A1
case: TC-A1
variant: v3
source: 02-3 Step 7 TC-A1 (v3)
adr: D5, D7

## Input
requiredHeaders: (none)
header: (none)
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
recordCount: 1
