# TC-INT-5 — a comment-only file yields a zero-column header: the required column is missing

## Meta
table: INT
rule: interaction
case: TC-INT-5
variant: v1
source: 02-3 Step 7 TC-INT-5
adr: D9

## Input
requiredHeaders: id
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)
commentMarker: #

## Data
```csv
# just a comment
```

## Expected
outcome: failure
phase: parse
throws: java.lang.IllegalArgumentException
message: The header is missing required name(s): [id] in []
