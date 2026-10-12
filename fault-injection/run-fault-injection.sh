#!/usr/bin/env bash
#
# Scheme 2 fault-injection runner:
#   inject (git apply the unified diff) -> run the whole approval suite
#   -> assert the exact red-fixture set and the message fragment
#   -> revert (git apply -R, git checkout fallback) -> assert the suite is green.
#
# Usage:  ./run-fault-injection.sh
# Exit :  0 = every fault closed the loop; 1 = at least one fault failed.
#
# Safety: `trap` restores the source on Ctrl-C / TERM / any exit.

set -u

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$HERE/.." && pwd)"
cd "$REPO" || exit 2

TEST_CLASS="org.apache.commons.csv.approved.RequiredHeadersApprovalTest"
REPORT="target/surefire-reports/TEST-${TEST_CLASS}.xml"
OUT="$HERE/out"
SUMMARY="$OUT/summary.txt"
SRC_FILES=(src/main/java/org/apache/commons/csv/CSVParser.java
           src/main/java/org/apache/commons/csv/CSVFormat.java)
mkdir -p "$OUT"
: > "$SUMMARY"

APPLIED=""

restore() {
  [ -n "$APPLIED" ] || return 0
  git apply -R "$APPLIED" 2>/dev/null || git checkout -- "${SRC_FILES[@]}" 2>/dev/null
  APPLIED=""
}
on_signal() { echo; echo "[trap] signal caught, restoring source..."; restore; trap - EXIT; exit 130; }
trap on_signal INT TERM
trap restore EXIT

log() { echo "$@" | tee -a "$SUMMARY"; }
# -Drat.skip=true: this one-off tool's own files have no ASF license headers, so
# Apache RAT would otherwise fail the build (we must not edit pom.xml to exclude them).
run_mvn() { mvn -o -q test -Drat.skip=true -Dtest="$TEST_CLASS" >"$1" 2>&1; }

# ---------------------------------------------------------------- preflight
if ! git diff --quiet || ! git diff --cached --quiet; then
  log "ABORT: tracked files have uncommitted changes. Commit or stash first."
  exit 2
fi
log "[preflight] baseline run..."
if ! run_mvn "$OUT/preflight.baseline.log"; then
  log "[preflight] ABORT: baseline is NOT green."
  exit 2
fi
log "[preflight] baseline green (40/40)."

FAIL=0
while IFS= read -r id; do
  [ -n "$id" ] || continue
  patch="$HERE/faults/patches/$id.patch"
  log ""
  log "================ $id ================"

  if ! git apply --check "$patch" 2>"$OUT/$id.apply.err"; then
    log "[$id] FAIL: git apply --check rejected the patch:"
    cat "$OUT/$id.apply.err" | tee -a "$SUMMARY"
    FAIL=$((FAIL + 1))
    continue
  fi

  git apply "$patch" && APPLIED="$patch"

  run_mvn "$OUT/$id.injected.log"
  python3 "$HERE/check.py" "$id" "$REPORT" >"$OUT/$id.check.txt" 2>&1
  check_rc=$?
  cat "$OUT/$id.check.txt" | tee -a "$SUMMARY"
  [ "$check_rc" -eq 0 ] || FAIL=$((FAIL + 1))

  # revert
  if git apply -R --check "$patch" 2>/dev/null; then
    git apply -R "$patch"
  else
    git checkout -- "${SRC_FILES[@]}"
  fi
  APPLIED=""
  if ! git diff --quiet; then
    log "[$id] FAIL: revert left the working tree dirty."
    FAIL=$((FAIL + 1))
  fi

  if run_mvn "$OUT/$id.restored.log"; then
    log "[$id] reverted and green again (40/40)."
  else
    log "[$id] FAIL: after revert the suite is NOT green."
    FAIL=$((FAIL + 1))
  fi
done < "$HERE/faults/ids.txt"

log ""
if [ "$FAIL" -eq 0 ]; then
  log "ALL FAULTS OK: inject -> red (exact set + fragment) -> revert -> green, for every fault."
else
  log "$FAIL fault-check(s) FAILED."
fi
exit $([ "$FAIL" -eq 0 ] && echo 0 || echo 1)
