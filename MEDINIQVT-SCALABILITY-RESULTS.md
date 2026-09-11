# MediniQVT — Scalability Results (All Examples)

Results from the tool-isolated scalability sweeps (`QVTScalability.sh`, driven by `scalability-lib.sh`) run on 2026-09-11, reading directly from each example's `scalability_results/` folder. Each measurement is the mean of `REPEAT=5` runs; `TIMEOUT=120s` per size (180s for familiestopersons) via JUnit's `assertTimeoutPreemptively`, with an additional OS-level `timeout`/SIGKILL wrapper per test class so a hung run cannot block the sweep.

**How to read a cell**: `n=<largest size reached>: <time>s, k≈<growth exponent>` — `k` is the local growth exponent between the two largest measured sizes (`k = ln(t2/t1) / ln(n2/n1)`; k≈1 linear, k≈2 quadratic). ⚠️ STALE marks a file whose timestamp predates this run (the size that hit it never produced a fresh measurement — see notes below).

**Summary**: MediniQVT tops out far below BXAgent/BXtend's n=100,000 on every growing-model axis (FWD/BWD/INCR_*/CDC*Sync) — typically n=100–10,000 depending on example, occasionally reaching 100,000 only where the direction is comparatively cheap (e.g. asttodag/bag1tobag2 BWD). Growth is quadratic-to-quartic in most examples (k≈2–5) versus BXAgent/BXtend's k≈0.3–1.9 on the same classes, consistent with MediniQVT's known 120s-timeout ceiling being hit well before six-figure sizes.

**ecoretosql / gantttocpm note**: `CMCSync`/`CMCFCSync` time out already at the smallest size (n=3) for these two examples, so `recordResult()`/`saveResults()` never ran and the result files still hold stale `0.0` data from an earlier (pre-`performAndPropagateEdit`) run — not a valid "instant" measurement. Treated as no-data below.

## Batch propagation (from-scratch build)

| Example | Forward (FWD) | Backward (BWD) |
|---|---|---|
| AST → DAG | n=5000: 22.2s, k≈1.91 | n=100000: 0.003s, k≈-0.42 |
| Bag1 → Bag2 | n=300: 20.9s, k≈3.82 | n=100000: 0.002s, k≈-0.58 |
| Ecore → SQL | n=1000: 17.3s, k≈2.89 | n=1000: 21.9s, k≈2.40 |
| Families → Persons | n=300: 1.23s, k≈2.51 | n=100: 1.95s, k≈2.84 |
| Gantt → CPM | n=100: 1.4s, k≈3.04 | n=10: 0.143s, k≈2.40 |
| PDB1 → PDB2 | n=10000: 16.8s, k≈2.06 | n=10000: 16.8s, k≈2.08 |
| Petrinet → PetrinetWeighted | n=5000: 17.5s, k≈2.02 | n=5000: 17.7s, k≈2.01 |
| Set → OSet | n=100: 1.66s, k≈3.34 | n=100: 1.63s, k≈3.65 |


## Incremental propagation (single edit after setup)

| Example | Incremental Fwd | Incremental Bwd |
|---|---|---|
| AST → DAG | n=10000: 0.279s, k≈0.86 | n=100000: 0.004s, k≈-0.32 |
| Bag1 → Bag2 | n=300: 20.8s, k≈3.72 | n=100000: 0.002s, k≈0.00 |
| Ecore → SQL | n=1000: 16s, k≈2.85 | n=1000: 21.6s, k≈2.34 |
| Families → Persons | n=1000: 41.4s, k≈2.89 | n=300: 51.3s, k≈2.93 |
| Gantt → CPM | n=300: 1.26s, k≈2.41 | n=10: 0.224s, k≈2.28 |
| PDB1 → PDB2 | n=10000: 11.8s, k≈1.96 | n=10000: 13.7s, k≈2.00 |
| Petrinet → PetrinetWeighted | n=5000: 50.3s, k≈2.05 | n=5000: 53.4s, k≈2.06 |
| Set → OSet | n=100: 1.7s, k≈3.14 | n=300: 103s, k≈3.88 |


## Concurrent sync — constant model, growing delta

Base model fixed at 100 elements; the number of concurrent edits grows.

| Example | Conflicting (CMCSync) | Conflict-free (CMCFCSync) |
|---|---|---|
| AST → DAG | n=50: 0.029s, k≈0.00 | n=100: 0.008s, k≈0.00 |
| Bag1 → Bag2 | n=50: 0.024s, k≈-2.40 | n=100: 0.486s, k≈1.66 |
| Ecore → SQL | n=50: <0.001s ⚠️ STALE | n=100: <0.001s ⚠️ STALE |
| Families → Persons | n=50: 1.92s, k≈0.19 | n=100: 4.32s, k≈0.41 |
| Gantt → CPM | n=50: <0.001s ⚠️ STALE | n=100: <0.001s ⚠️ STALE |
| PDB1 → PDB2 | n=50: 0.013s, k≈-0.33 | n=100: 0.047s, k≈0.63 |
| Petrinet → PetrinetWeighted | n=50: 0.023s, k≈-0.55 | n=100: 0.068s, k≈0.72 |
| Set → OSet | n=50: 0.273s, k≈-3.03 | n=60: 11.2s, k≈1.47 |


## Concurrent sync — constant delta, growing model

Edit size fixed; the base model grows.

| Example | Conflicting (CDCSync) | Conflict-free (CDCFCSync) |
|---|---|---|
| AST → DAG | n=10000: 117s, k≈2.18 | n=10000: 0.326s, k≈0.87 |
| Bag1 → Bag2 | n=50: 0.043s, k≈1.61 | n=100: 0.322s, k≈2.83 |
| Ecore → SQL | n=30: 37.5s, k≈4.66 | n=30: 38.6s, k≈4.64 |
| Families → Persons | n=50: 1.68s, k≈2.33 | n=100: 2.16s, k≈2.84 |
| Gantt → CPM | n=40: 100s, k≈4.96 | n=30: 39.3s, k≈4.14 |
| PDB1 → PDB2 | n=10000: 24.8s, k≈1.99 | n=10000: 27.8s, k≈2.04 |
| Petrinet → PetrinetWeighted | n=5000: 95.7s, k≈2.06 | n=5000: 103s, k≈2.09 |
| Set → OSet | n=50: 0.236s, k≈3.11 | n=100: 1.85s, k≈3.76 |


## Data caveats

**Stale files** (present but not refreshed by this run — excluded from the growth-exponent reading above, shown only because a result file happens to exist at that path):

- Ecore → SQL / `CMCSync` — `CMCSync_MediniQVT.txt` is 24 days old
- Ecore → SQL / `CMCFCSync` — `CMCFCSync_MediniQVT.txt` is 24 days old
- Gantt → CPM / `CMCSync` — `CMCSync_MediniQVT.txt` is 24 days old
- Gantt → CPM / `CMCFCSync` — `CMCFCSync_MediniQVT.txt` is 24 days old

