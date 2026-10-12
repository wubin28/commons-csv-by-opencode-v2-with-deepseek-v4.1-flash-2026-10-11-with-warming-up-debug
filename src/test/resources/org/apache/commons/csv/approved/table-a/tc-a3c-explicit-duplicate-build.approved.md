# TC-A3c — an explicit duplicate header fails while building the format

## Meta
table: A
rule: R-A3
case: TC-A3c
variant: v1
source: 02-3 Step 7 TC-A3c
adr: D3, D8

## Input
requiredHeaders: currency
header: id, id
ignoreHeaderCase: (default)
duplicateHeaderMode: DISALLOW
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
```

## Expected
outcome: failure
phase: build
throws: java.lang.IllegalArgumentException
message: The header contains a duplicate name: "id" in [id, id]. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().
