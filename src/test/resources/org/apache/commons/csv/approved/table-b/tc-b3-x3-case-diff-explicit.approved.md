# TC-B3 x3 — the case-only-difference case through the explicit-header path

## Meta
table: B
rule: R-B3
case: TC-B3
variant: x3
source: boundary added while implementing (explicit-header variant)
adr: D2

## Input
requiredHeaders: currency
header: Currency
ignoreHeaderCase: true
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
100
```

## Expected
outcome: success
headerNames: Currency
recordCount: 1
record[0].get(currency): 100
