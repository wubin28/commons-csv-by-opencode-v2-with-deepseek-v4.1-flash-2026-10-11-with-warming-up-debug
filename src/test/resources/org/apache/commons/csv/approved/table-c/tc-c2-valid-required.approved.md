# TC-C2 — legal required headers are stored and returned defensively

## Meta
table: C
rule: R-C2
case: TC-C2
variant: v1
source: 02-3 Step 7 TC-C2
adr: D7, D10

## Input
requiredHeaders: currency, amount
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
currency,amount
100,200
```

## Expected
outcome: success
formatRequiredHeaders: currency, amount
headerNames: currency, amount
headerMapSize: 2
recordCount: 1
record[0].get(currency): 100
