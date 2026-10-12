# TC-A3b — a duplicate header column fails before the missing-column check

## Meta
table: A
rule: R-A3
case: TC-A3b
variant: v1
source: 02-3 Step 7 TC-A3b
adr: D3, D5, D8

## Input
requiredHeaders: currency
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: DISALLOW
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,id
1,2
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header contains a duplicate name: "id" in [id, id]. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().
