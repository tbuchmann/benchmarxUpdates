# BXAgent — Scalability Results (All Examples)

Results from the tool-isolated scalability sweeps (`BXAgentScalability.sh`, driven by `scalability-lib.sh`) run on 2026-09-11, reading directly from each example's `scalability_results/` folder. Each measurement is the mean of `REPEAT=5` runs; `TIMEOUT=120s` per size (180s for familiestopersons) via JUnit's `assertTimeoutPreemptively`, with an additional OS-level `timeout`/SIGKILL wrapper per test class so a hung run cannot block the sweep.

**How to read a cell**: `n=<largest size reached>: <time>s, k≈<growth exponent>` — `k` is the local growth exponent between the two largest measured sizes (`k = ln(t2/t1) / ln(n2/n1)`; k≈1 linear, k≈2 quadratic). ⚠️ STALE marks a file whose timestamp predates this run (the size that hit it never produced a fresh measurement — see notes below).

**Summary**: all 8 examples reach the full `n=100,000` target on every batch/incremental/`CDC*Sync` class, no timeouts, roughly linear-to-mildly-superlinear growth throughout (k mostly 0.3–1.8). The constant-model concurrent classes (`CMCSync`/`CMCFCSync`) cap at n=50/100 by test design, not by any limitation — all sub-10ms.

## Batch propagation (from-scratch build)

| Example | Forward (FWD) | Backward (BWD) |
|---|---|---|
| AST → DAG | n=100000: 3.05s, k≈1.36 | n=100000: 0.774s, k≈0.98 |
| Bag1 → Bag2 | n=100000: 0.32s, k≈0.90 | n=100000: 0.263s, k≈0.87 |
| Ecore → SQL | n=100000: 1.73s, k≈0.93 | n=100000: 2.75s, k≈0.90 |
| Families → Persons | n=100000: 1.31s, k≈0.97 | n=100000: 1.36s, k≈1.04 |
| Gantt → CPM | n=100000: 3.3s, k≈1.71 | n=100000: 0.385s, k≈0.58 |
| PDB1 → PDB2 | n=100000: 0.403s, k≈0.93 | n=100000: 0.403s, k≈0.85 |
| Petrinet → PetrinetWeighted | n=100000: 1.13s, k≈1.07 | n=100000: 1.09s, k≈1.15 |
| Set → OSet | n=100000: 5.55s, k≈1.79 | n=100000: 0.287s, k≈0.97 |


## Incremental propagation (single edit after setup)

| Example | Incremental Fwd | Incremental Bwd |
|---|---|---|
| AST → DAG | n=100000: 0.893s, k≈0.99 | n=100000: 0.71s, k≈1.02 |
| Bag1 → Bag2 | n=100000: 0.158s, k≈0.33 | n=100000: 0.1s, k≈0.10 |
| Ecore → SQL | n=100000: 1.73s, k≈1.22 | n=100000: 2.56s, k≈0.90 |
| Families → Persons | n=100000: 1.08s, k≈1.35 | n=100000: 1.63s, k≈0.93 |
| Gantt → CPM | n=100000: 0.14s, k≈0.40 | n=100000: 0.176s, k≈0.35 |
| PDB1 → PDB2 | n=100000: 0.181s, k≈0.89 | n=100000: 0.177s, k≈0.99 |
| Petrinet → PetrinetWeighted | n=100000: 0.681s, k≈0.70 | n=100000: 0.64s, k≈0.99 |
| Set → OSet | n=100000: 0.189s, k≈0.81 | n=100000: 0.195s, k≈1.13 |


## Concurrent sync — constant model, growing delta

Base model fixed at 100 elements; the number of concurrent edits grows.

| Example | Conflicting (CMCSync) | Conflict-free (CMCFCSync) |
|---|---|---|
| AST → DAG | n=50: 0.003s, k≈0.00 | n=100: 0.002s, k≈-3.85 |
| Bag1 → Bag2 | n=50: 0.001s, k≈0.00 | n=100: 0.002s, k≈0.00 |
| Ecore → SQL | n=50: 0.005s, k≈0.00 | n=100: 0.005s, k≈0.00 |
| Families → Persons | n=50: 0.012s, k≈0.82 | n=100: 0.021s, k≈0.95 |
| Gantt → CPM | n=50: 0.033s, k≈-1.08 | n=100: 0.102s, k≈1.29 |
| PDB1 → PDB2 | n=50: 0.001s, k≈0.00 | n=100: 0.003s, k≈3.85 |
| Petrinet → PetrinetWeighted | n=50: 0.002s, k≈-1.82 | n=100: 0.003s, k≈0.00 |
| Set → OSet | n=50: 0.003s, k≈1.82 | n=100: 0.001s, k≈0.00 |


## Concurrent sync — constant delta, growing model

Edit size fixed; the base model grows.

| Example | Conflicting (CDCSync) | Conflict-free (CDCFCSync) |
|---|---|---|
| AST → DAG | n=100000: 1.73s, k≈0.98 | n=100000: 1.26s, k≈0.91 |
| Bag1 → Bag2 | n=100000: 0.277s, k≈0.43 | n=100000: 0.29s, k≈0.56 |
| Ecore → SQL | n=100000: 3.84s, k≈0.89 | n=100000: 4.04s, k≈0.99 |
| Families → Persons | n=100000: 6.91s, k≈0.82 | n=100000: 3.41s, k≈0.65 |
| Gantt → CPM | n=100000: 0.544s, k≈0.82 | n=100000: 0.437s, k≈0.38 |
| PDB1 → PDB2 | n=100000: 0.289s, k≈0.37 | n=100000: 0.335s, k≈0.62 |
| Petrinet → PetrinetWeighted | n=100000: 1.53s, k≈0.53 | n=100000: 1.6s, k≈0.56 |
| Set → OSet | n=100000: 0.48s, k≈0.62 | n=100000: 0.336s, k≈0.64 |

