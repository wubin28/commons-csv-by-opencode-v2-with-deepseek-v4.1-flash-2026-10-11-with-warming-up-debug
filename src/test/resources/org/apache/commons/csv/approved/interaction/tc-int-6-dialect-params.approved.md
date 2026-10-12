# TC-INT-6 — dialect parameters do not affect the required-column verdict

## Meta
table: INT
rule: interaction
case: TC-INT-6
variant: v1
source: 02-3 Step 7 TC-INT-6
adr: D2

## Input
requiredHeaders: id
header: (empty)
ignoreHeaderCase: (default)
duplicateHeaderMode: (default)
allowMissingColumnNames: (default)
skipHeaderRecord: (default)
delimiter: ;
quote: "
trim: true
nullString: NULL

## Data
```csv
id;name
1;NULL
```

## Expected
outcome: success
headerNames: id, name
recordCount: 1
record[0].get(id): 1
record[0].get(name): (null)
