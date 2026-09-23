#!/usr/bin/env bash
# scalability-lib.sh — shared driver for the per-tool scalability sweeps
# (BXAgentScalability.sh / BXtendScalability.sh / QVTScalability.sh source this).
#
# Why the OS-level timeout wrapper below exists:
#   Each scalability measurement is bounded in-process by JUnit's
#   assertTimeoutPreemptively(TIMEOUT) (120s; 180s for familiestopersons).
#   That raises a timeout *assertion* but does NOT kill a non-cooperative
#   worker thread. MediniQVT in particular spawns threads that ignore the
#   interrupt, so the surefire fork never exits and `./mvnw` would hang
#   forever (surefire's own forkedProcessTimeoutInSeconds is not configured
#   either). We therefore run every `mvn` call under `timeout` and SIGKILL
#   any surefire fork left behind afterwards.
#
# Usage from a wrapper script:
#   source "$(dirname "${BASH_SOURCE[0]}")/scalability-lib.sh"
#   run_sweep <label> <module-path> <ToolClassSimpleName>  [<module-path> <tool> ...]
#
# Env overrides:
#   SCALABILITY_CEILING   hard wall per test class, seconds        (default 300)
#   SCALABILITY_GRACE     SIGKILL delay after SIGTERM, seconds     (default 30)
#   SCALABILITY_CLASSES   space-separated class list to run        (default: all 8)

set -u -o pipefail            # deliberately NOT -e: one bad example must not abort the sweep

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

DEFAULT_CLASSES=(
  ScalabilityBatchTestsFwd
  ScalabilityBatchTestsBwd
  ScalabilityIncrTestsFwd
  ScalabilityIncrTestsBwd
  ScalabilityConstDeltaCSync
  ScalabilityConstDeltaCFCSync
  ScalabilityConstModelCSync
  ScalabilityConstModelCFCSync
)
if [[ -n ${SCALABILITY_CLASSES:-} ]]; then
  read -r -a CLASSES <<< "$SCALABILITY_CLASSES"
else
  CLASSES=("${DEFAULT_CLASSES[@]}")
fi

CEILING="${SCALABILITY_CEILING:-300}"    # seconds, hard wall per test class
GRACE="${SCALABILITY_GRACE:-30}"         # seconds between SIGTERM and SIGKILL

STAMP="$(date +%Y%m%d-%H%M%S)"
LOG_DIR="$ROOT/scalability_logs"
mkdir -p "$LOG_DIR"

# Kill any surefire fork still running for the given module (best effort).
# The forked JVM's command line contains the module's target/surefire path,
# so we match on the module directory name.
_reap_forks() {
  local dir="${1##*/}"
  pkill -9 -f "surefire.*/${dir}/target/surefire" 2>/dev/null || true
  pkill -9 -f "surefirebooter.*${dir}"            2>/dev/null || true
}

# run_sweep <label> [<module-path> <ToolClassSimpleName>]...
run_sweep() {
  local label="$1"; shift
  local summary="$LOG_DIR/${label}-${STAMP}.summary"
  {
    echo "sweep    : $label"
    echo "started  : $(date)"
    echo "ceiling  : ${CEILING}s per class   grace: ${GRACE}s"
    echo "classes  : ${CLASSES[*]}"
    echo
  } | tee "$summary"

  while (( $# )); do
    local module="$1" tool="$2"; shift 2
    if [[ ! -d "$module" ]]; then
      printf 'SKIP  %-28s (module dir missing)\n' "${module##*/}" | tee -a "$summary"
      continue
    fi
    for cls in "${CLASSES[@]}"; do
      local log="$LOG_DIR/${label}-${module##*/}-${cls}-${STAMP}.log"
      local t0=$SECONDS
      printf '  ... %-28s %-30s ' "${module##*/}" "$cls" >&2
      # -am is required: without it the module resolves stale core/metamodel
      # artifacts from ~/.m2 and the tests NPE on a null source model.
      timeout -k "$GRACE" "$CEILING" \
        ./mvnw -q -pl "$module" -am test \
          -Dbenchmarx.tool="$tool" \
          -Dtest="$cls" \
          -Dsurefire.failIfNoSpecifiedTests=false \
          > "$log" 2>&1
      local rc=$? dt=$(( SECONDS - t0 ))
      _reap_forks "$module"
      local tag
      case $rc in
        0)   tag="ok   " ;;
        1)   tag="part " ;;   # mvn rc=1 => surefire test failures: expected when a
                              # size hits its limit or the tool lacks the operation;
                              # partial results are still written to scalability_results/
        124) tag="KILL " ;;   # timeout sent SIGTERM at the ceiling
        137) tag="KILL9" ;;   # needed SIGKILL after the grace period
        *)   tag="rc$rc " ;;
      esac
      printf '%s  %ds\n' "$tag" "$dt" >&2
      printf '%-5s %-28s %-30s %5ds   (rc=%d, %s)\n' \
        "$tag" "${module##*/}" "$cls" "$dt" "$rc" "${log#"$ROOT"/}" | tee -a "$summary"
    done
  done

  {
    echo
    echo "finished : $(date)"
    echo "results  : scalability_results/*.txt   (per example dir)"
    echo "logs     : $LOG_DIR/${label}-*-${STAMP}.log"
  } | tee -a "$summary"

  if pgrep -af 'surefirebooter' >/dev/null 2>&1; then
    echo "WARNING  : stray surefire fork(s) still alive:" | tee -a "$summary"
    pgrep -af 'surefirebooter' | tee -a "$summary"
  fi
}
