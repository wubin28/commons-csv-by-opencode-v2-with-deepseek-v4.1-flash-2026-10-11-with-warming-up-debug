# TC-CFG-1 — getRequiredHeaders returns a defensive copy

## Meta
table: CFG
rule: config
case: TC-CFG-1
variant: v1
source: 02-3 Step 7 TC-CFG-1
adr: D10

## Input
requiredHeaders: a, b
header: (none)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
```

## Expected
outcome: config
formatRequiredHeaders: a, b
defensiveCopyHolds: true
