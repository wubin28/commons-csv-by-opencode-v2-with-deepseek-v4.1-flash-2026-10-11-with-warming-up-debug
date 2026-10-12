# TC-B2 x2 — the required column is a subset of the actual columns: pass

## Meta
table: B
rule: R-B2
case: TC-B2
variant: x2
source: boundary added while implementing
adr: D2

## Input
requiredHeaders: id
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,name,extra
1,2,3
```

## Expected
outcome: success
headerNames: id, name, extra
recordCount: 1
record[0].get(id): 1
