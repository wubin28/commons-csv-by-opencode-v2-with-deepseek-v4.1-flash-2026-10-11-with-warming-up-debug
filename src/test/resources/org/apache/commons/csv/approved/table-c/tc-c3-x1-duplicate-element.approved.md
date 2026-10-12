# TC-C3 x1 — duplicate required names are de-duplicated in the message

## Meta
table: C
rule: R-C3
case: TC-C3
variant: x1
source: boundary added while implementing (ADR D7d)
adr: D7d, D8

## Input
requiredHeaders: currency, currency
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)

## Data
```csv
id,name
1,2
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [currency] in [id, name]
