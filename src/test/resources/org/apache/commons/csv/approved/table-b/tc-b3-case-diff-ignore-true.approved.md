# TC-B3 — the required column differs only by case and ignoreHeaderCase=true: pass

## Meta
table: B
rule: R-B3
case: TC-B3
variant: v1
source: 02-3 Step 7 TC-B3
adr: D2

## Input
requiredHeaders: currency
header: (empty)
ignoreHeaderCase: true
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
Currency
100
```

## Expected
outcome: success
headerNames: Currency
recordCount: 1
record[0].get(currency): 100
