# BXtend — Scalability Results (All Examples)

Results from the tool-isolated scalability sweeps (`BXtendScalability.sh`, driven by `scalability-lib.sh`) run on 2026-09-11, reading directly from each example's `scalability_results/` folder. Each measurement is the mean of `REPEAT=5` runs; `TIMEOUT=120s` per size (180s for familiestopersons) via JUnit's `assertTimeoutPreemptively`, with an additional OS-level `timeout`/SIGKILL wrapper per test class so a hung run cannot block the sweep.

**How to read a cell**: `n=<largest size reached>: <time>s, k≈<growth exponent>` — `k` is the local growth exponent between the two largest measured sizes (`k = ln(t2/t1) / ln(n2/n1)`; k≈1 linear, k≈2 quadratic). ⚠️ STALE marks a file whose timestamp predates this run (the size that hit it never produced a fresh measurement — see notes below).

**Summary**: for the 7 examples with fresh data, results closely mirror BXAgent — full `n=100,000` reached on every batch/incremental/`CDC*Sync` class, near-linear growth (k mostly 0.6–1.9), sub-millisecond `CMCSync`/`CMCFCSync` (capped at n=50/100 by design). BXtend now implements concurrent sync everywhere, not just familiestopersons — see below for why familiestopersons itself has no fresh reading.

**familiestopersons note**: the BXtend sweep produced no fresh output for this example — every `*_BXtend Synch.txt` file in its `scalability_results/` folder predates 2026-09-11 (the adapter's `getName()` returns `"BXtend Synch"` here, not `"BXtend"`, so its files are named differently from the other 7 examples too). The stale numbers are omitted from the tables below rather than presented as current data — re-run `BXtendScalability.sh` limited to familiestopersons to get a fresh reading.

## Batch propagation (from-scratch build)

| Example | Forward (FWD) | Backward (BWD) |
|---|---|---|
| AST → DAG | n=100000: 0.819s, k≈1.10 | n=100000: 0.357s, k≈1.16 |
| Bag1 → Bag2 | n=100000: 0.035s, k≈1.13 | n=100000: 0.023s, k≈1.06 |
| Ecore → SQL | n=100000: 1.35s, k≈0.77 | n=100000: 1.08s, k≈1.07 |
| Families → Persons | n=100000: 1.23s, k≈1.04 ⚠️ STALE | n=100000: 2.13s ⚠️ STALE |
| Gantt → CPM | n=100000: 0.197s, k≈1.20 | n=100000: 0.197s, k≈1.13 |
| PDB1 → PDB2 | n=100000: 0.182s, k≈0.92 | n=100000: 0.152s, k≈1.04 |
| Petrinet → PetrinetWeighted | n=100000: 0.255s, k≈1.08 | n=100000: 0.266s, k≈0.89 |
| Set → OSet | n=100000: 0.169s, k≈1.82 | n=100000: 0.106s, k≈1.20 |


## Incremental propagation (single edit after setup)

| Example | Incremental Fwd | Incremental Bwd |
|---|---|---|
| AST → DAG | n=100000: 0.872s, k≈1.11 | n=100000: 0.499s, k≈1.06 |
| Bag1 → Bag2 | n=100000: 0.03s, k≈0.58 | n=100000: 0.003s, k≈0.00 |
| Ecore → SQL | n=100000: 1.21s, k≈1.08 | n=100000: 1.13s, k≈1.09 |
| Families → Persons | n=300: 0.004s, k≈1.26 ⚠️ STALE | n=300: 0.002s, k≈0.63 ⚠️ STALE |
| Gantt → CPM | n=100000: 0.061s, k≈1.23 | n=100000: 0.06s, k≈0.91 |
| PDB1 → PDB2 | n=100000: 0.057s, k≈0.66 | n=100000: 0.047s, k≈0.60 |
| Petrinet → PetrinetWeighted | n=100000: 0.345s, k≈1.94 | n=100000: 0.341s, k≈1.51 |
| Set → OSet | n=100000: 0.034s, k≈0.70 | n=100000: 0.031s, k≈1.25 |


## Concurrent sync — constant model, growing delta

Base model fixed at 100 elements; the number of concurrent edits grows.

| Example | Conflicting (CMCSync) | Conflict-free (CMCFCSync) |
|---|---|---|
| AST → DAG | n=50: 0.001s, k≈0.00 | n=100: 0.001s, k≈0.00 |
| Bag1 → Bag2 | n=50: <0.001s | n=100: <0.001s |
| Ecore → SQL | n=50: 0.003s, k≈0.00 | n=100: 0.004s, k≈0.00 |
| Families → Persons | n=50: 0.484s, k≈1.60 ⚠️ STALE | n=100: 1.97s, k≈2.04 ⚠️ STALE |
| Gantt → CPM | n=50: <0.001s | n=100: <0.001s |
| PDB1 → PDB2 | n=50: 0.006s, k≈0.82 | n=100: <0.001s |
| Petrinet → PetrinetWeighted | n=50: <0.001s | n=100: <0.001s |
| Set → OSet | n=50: 0.008s, k≈0.00 | n=100: <0.001s |


## Concurrent sync — constant delta, growing model

Edit size fixed; the base model grows.

| Example | Conflicting (CDCSync) | Conflict-free (CDCFCSync) |
|---|---|---|
| AST → DAG | n=100000: 1s, k≈0.92 | n=100000: 1.35s, k≈0.93 |
| Bag1 → Bag2 | n=100000: 0.023s, k≈0.62 | n=100000: 0.019s, k≈0.16 |
| Ecore → SQL | n=100000: 2.76s, k≈0.91 | n=100000: 2.88s, k≈1.00 |
| Families → Persons | n=50: 0.014s, k≈1.51 ⚠️ STALE | n=100: 0.029s, k≈1.41 ⚠️ STALE |
| Gantt → CPM | n=100000: 0.108s, k≈0.90 | n=100000: 0.112s, k≈0.97 |
| PDB1 → PDB2 | n=100000: 0.512s, k≈1.66 | n=100000: 0.083s, k≈0.76 |
| Petrinet → PetrinetWeighted | n=100000: 0.64s, k≈1.45 | n=100000: 0.646s, k≈1.36 |
| Set → OSet | n=100000: 0.407s, k≈0.95 | n=100000: 0.072s, k≈0.44 |


## Data caveats

**Stale files** (present but not refreshed by this run — excluded from the growth-exponent reading above, shown only because a result file happens to exist at that path):

- Families → Persons / `FWD` — `FWD_BXtend Synch.txt` is 293 days old
- Families → Persons / `BWD` — `BWD_BXtend Synch.txt` is 286 days old
- Families → Persons / `INCR_FWD` — `INCR_FWD_BXtend Synch.txt` is 333 days old
- Families → Persons / `INCR_BWD` — `INCR_BWD_BXtend Synch.txt` is 333 days old
- Families → Persons / `CMCSync` — `CMCSync_BXtend Synch.txt` is 333 days old
- Families → Persons / `CMCFCSync` — `CMCFCSync_BXtend Synch.txt` is 333 days old
- Families → Persons / `CDCSync` — `CDCsync_BXtend Synch.txt` is 333 days old
- Families → Persons / `CDCFCSync` — `CDCFCSync_BXtend Synch.txt` is 333 days old

