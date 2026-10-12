# TC-CFG-3 — two formats differing only in required headers are not equal

## Meta
table: CFG
rule: config
case: TC-CFG-3
variant: v1
source: 02-3 Step 7 TC-CFG-3
adr: D10

## Input
requiredHeaders: a
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
equalsDiffersOnRequiredHeaders: true
