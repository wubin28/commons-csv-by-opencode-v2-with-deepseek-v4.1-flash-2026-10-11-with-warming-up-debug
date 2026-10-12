# TC-CFG-2 — copy() and builder() carry the required headers over

## Meta
table: CFG
rule: config
case: TC-CFG-2
variant: v1
source: 02-3 Step 7 TC-CFG-2
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
copyPreservesRequiredHeaders: true
