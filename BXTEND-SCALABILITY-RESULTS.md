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

## Raw data (all measured points, per example)

Every line below is copied verbatim from the corresponding `scalability_results/<label>_<tool>.txt` file (`size, seconds` per line, mean of `REPEAT=5` runs) — nothing is filtered or downsampled. Use this section as the data source for scatterplots; the tables above are only a condensed summary.


### AST → DAG (`asttodag`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:05 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.001
50, 0.002
100, 0.001
300, 0.004
1000, 0.009
5000, 0.036
10000, 0.049
50000, 0.382
100000, 0.819
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:05 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.001
50, 0.001
100, 0.001
1000, 0.007
5000, 0.02
10000, 0.034
50000, 0.16
100000, 0.357
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:05 (0.9 days ago) -->
```
3, 0.002
5, 0.001
10, 0.001
50, 0.002
100, 0.003
300, 0.005
1000, 0.009
5000, 0.06
10000, 0.091
50000, 0.405
100000, 0.872
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:05 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.002
50, 0.003
100, 0.004
1000, 0.01
5000, 0.042
10000, 0.054
50000, 0.239
100000, 0.499
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.001
20, 0.001
30, 0.001
40, 0.001
50, 0.001
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.006
5, 0.003
10, 0.001
20, 0.002
30, 0.002
40, 0.002
50, 0.002
60, 0.002
70, 0.001
80, 0.001
90, 0.001
100, 0.001
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.009
5, 0.002
10, 0.001
20, 0.002
30, 0.001
40, 0.001
50, 0.002
1000, 0.027
5000, 0.069
10000, 0.105
50000, 0.528
100000, 1.0
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.001
20, 0.001
30, 0.002
40, 0.002
50, 0.003
60, 0.002
70, 0.002
80, 0.002
90, 0.003
100, 0.002
1000, 0.014
5000, 0.08
10000, 0.153
50000, 0.71
100000, 1.349
```


### Bag1 → Bag2 (`bag1tobag2`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.0
100, 0.0
300, 0.001
1000, 0.001
5000, 0.005
10000, 0.006
50000, 0.016
100000, 0.035
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.0
1000, 0.001
5000, 0.004
10000, 0.004
50000, 0.011
100000, 0.023
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.001
1000, 0.001
5000, 0.004
10000, 0.005
50000, 0.02
100000, 0.03
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:06 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.0
50, 0.0
100, 0.0
300, 0.0
1000, 0.0
5000, 0.002
10000, 0.001
50000, 0.003
100000, 0.003
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:07 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:07 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
60, 0.0
70, 0.0
80, 0.0
90, 0.0
100, 0.0
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXtend.txt, last written 2026-09-11 00:07 (0.9 days ago) -->
```
3, 0.003
5, 0.0
10, 0.001
20, 0.0
30, 0.0
40, 0.0
50, 0.0
1000, 0.0
5000, 0.002
10000, 0.003
50000, 0.015
100000, 0.023
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:07 (0.9 days ago) -->
```
3, 0.007
5, 0.0
10, 0.0
20, 0.0
30, 0.001
40, 0.0
50, 0.0
60, 0.001
70, 0.001
80, 0.0
90, 0.001
100, 0.001
1000, 0.001
5000, 0.003
10000, 0.003
50000, 0.017
100000, 0.019
```


### Ecore → SQL (`ecoretosql`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:07 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.003
300, 0.01
1000, 0.02
5000, 0.076
10000, 0.126
50000, 0.791
100000, 1.347
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:08 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.002
1000, 0.012
5000, 0.054
10000, 0.108
50000, 0.511
100000, 1.076
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:08 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.004
300, 0.005
1000, 0.011
5000, 0.056
10000, 0.106
50000, 0.571
100000, 1.207
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:08 (0.9 days ago) -->
```
3, 0.002
5, 0.001
10, 0.002
50, 0.003
100, 0.004
1000, 0.016
5000, 0.054
10000, 0.112
50000, 0.528
100000, 1.127
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:09 (0.9 days ago) -->
```
3, 0.005
5, 0.003
10, 0.003
20, 0.003
30, 0.003
40, 0.003
50, 0.003
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:09 (0.9 days ago) -->
```
3, 0.006
5, 0.003
10, 0.003
20, 0.003
30, 0.003
40, 0.004
50, 0.005
60, 0.003
70, 0.004
80, 0.004
90, 0.004
100, 0.004
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXtend.txt, last written 2026-09-11 00:08 (0.9 days ago) -->
```
3, 0.005
5, 0.002
10, 0.003
20, 0.004
30, 0.003
40, 0.003
50, 0.003
1000, 0.029
5000, 0.168
10000, 0.252
50000, 1.468
100000, 2.759
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:08 (0.9 days ago) -->
```
3, 0.006
5, 0.002
10, 0.003
20, 0.003
30, 0.003
40, 0.003
50, 0.003
60, 0.002
70, 0.003
80, 0.003
90, 0.002
100, 0.003
1000, 0.028
5000, 0.157
10000, 0.264
50000, 1.445
100000, 2.885
```


### Families → Persons (`familiestopersons`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend Synch.txt, last written 2025-11-22 10:11 (293.4 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 293 days; not fresh data.

```
3, 0.002
5, 0.001
10, 0.001
50, 0.005
100, 0.005
300, 0.009
500, 0.01
1000, 0.012
3000, 0.046
5000, 0.041
10000, 0.112
100000, 1.226
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend Synch.txt, last written 2025-11-29 08:43 (286.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 286 days; not fresh data.

```
100000, 2.131
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.0
5, 0.0
10, 0.0
50, 0.0
100, 0.001
300, 0.004
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.0
5, 0.0
10, 0.0
50, 0.0
100, 0.001
300, 0.002
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.004
5, 0.014
10, 0.043
20, 0.114
30, 0.217
40, 0.339
50, 0.484
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.018
5, 0.028
10, 0.05
20, 0.148
30, 0.262
40, 0.385
50, 0.56
60, 0.77
70, 1.022
80, 1.291
90, 1.589
100, 1.97
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.0
5, 0.002
10, 0.002
20, 0.003
30, 0.006
40, 0.01
50, 0.014
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend Synch.txt, last written 2025-10-13 09:54 (333.5 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 334 days; not fresh data.

```
3, 0.0
5, 0.0
10, 0.002
20, 0.002
30, 0.003
40, 0.01
50, 0.008
60, 0.012
70, 0.015
80, 0.02
90, 0.025
100, 0.029
```


### Gantt → CPM (`gantttocpm`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.002
1000, 0.003
5000, 0.014
10000, 0.019
50000, 0.086
100000, 0.197
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
1000, 0.002
5000, 0.012
10000, 0.015
50000, 0.09
100000, 0.197
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.001
50, 0.001
100, 0.001
300, 0.001
1000, 0.002
5000, 0.005
10000, 0.011
50000, 0.026
100000, 0.061
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.0
50, 0.0
100, 0.001
1000, 0.003
5000, 0.005
10000, 0.011
50000, 0.032
100000, 0.06
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
60, 0.0
70, 0.0
80, 0.001
90, 0.0
100, 0.0
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.007
5, 0.001
10, 0.0
20, 0.0
30, 0.0
40, 0.001
50, 0.0
1000, 0.005
5000, 0.007
10000, 0.011
50000, 0.058
100000, 0.108
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:10 (0.9 days ago) -->
```
3, 0.004
5, 0.0
10, 0.001
20, 0.0
30, 0.001
40, 0.001
50, 0.001
60, 0.001
70, 0.001
80, 0.0
90, 0.001
100, 0.001
1000, 0.001
5000, 0.005
10000, 0.015
50000, 0.057
100000, 0.112
```


### PDB1 → PDB2 (`pdb1topdb2`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.001
1000, 0.003
5000, 0.011
10000, 0.018
50000, 0.096
100000, 0.182
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.001
50, 0.001
100, 0.001
1000, 0.004
5000, 0.016
10000, 0.018
50000, 0.074
100000, 0.152
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.0
50, 0.001
100, 0.001
300, 0.001
1000, 0.002
5000, 0.004
10000, 0.009
50000, 0.036
100000, 0.057
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.0
1000, 0.002
5000, 0.003
10000, 0.01
50000, 0.031
100000, 0.047
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.004
5, 0.002
10, 0.003
20, 0.003
30, 0.004
40, 0.005
50, 0.006
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.0
20, 0.0
30, 0.001
40, 0.001
50, 0.001
60, 0.001
70, 0.001
80, 0.001
90, 0.0
100, 0.0
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.007
5, 0.001
10, 0.001
20, 0.001
30, 0.0
40, 0.001
50, 0.001
1000, 0.008
5000, 0.017
10000, 0.031
50000, 0.162
100000, 0.512
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:11 (0.9 days ago) -->
```
3, 0.003
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.001
50, 0.0
60, 0.0
70, 0.0
80, 0.001
90, 0.0
100, 0.001
1000, 0.003
5000, 0.008
10000, 0.013
50000, 0.049
100000, 0.083
```


### Petrinet → PetrinetWeighted (`pntopnw`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.002
1000, 0.004
5000, 0.01
10000, 0.018
50000, 0.121
100000, 0.255
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
1000, 0.005
5000, 0.015
10000, 0.026
50000, 0.144
100000, 0.266
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.001
50, 0.001
100, 0.002
300, 0.003
1000, 0.004
5000, 0.014
10000, 0.017
50000, 0.09
100000, 0.345
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.002
100, 0.002
300, 0.003
1000, 0.008
5000, 0.019
10000, 0.021
50000, 0.12
100000, 0.341
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.0
20, 0.001
30, 0.001
40, 0.001
50, 0.0
60, 0.0
70, 0.001
80, 0.001
90, 0.001
100, 0.0
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXtend.txt, last written 2026-09-11 00:12 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.001
20, 0.001
30, 0.001
40, 0.001
50, 0.001
1000, 0.007
5000, 0.023
10000, 0.058
50000, 0.234
100000, 0.64
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.0
20, 0.001
30, 0.001
40, 0.001
50, 0.002
60, 0.002
70, 0.002
80, 0.001
90, 0.001
100, 0.001
1000, 0.006
5000, 0.028
10000, 0.041
50000, 0.252
100000, 0.646
```


### Set → OSet (`settooset`)


#### Batch Forward (FWD)

<!-- source: FWD_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.001
1000, 0.003
5000, 0.009
10000, 0.016
50000, 0.048
100000, 0.169
```


#### Batch Backward (BWD)

<!-- source: BWD_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.0
1000, 0.003
5000, 0.008
10000, 0.012
50000, 0.046
100000, 0.106
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.001
50, 0.001
100, 0.001
300, 0.001
1000, 0.001
5000, 0.004
10000, 0.008
50000, 0.021
100000, 0.034
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.001
50, 0.001
100, 0.0
300, 0.0
1000, 0.001
5000, 0.003
10000, 0.005
50000, 0.013
100000, 0.031
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXtend.txt, last written 2026-09-11 00:14 (0.9 days ago) -->
```
3, 0.005
5, 0.004
10, 0.003
20, 0.005
30, 0.008
40, 0.008
50, 0.008
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXtend.txt, last written 2026-09-11 00:14 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
60, 0.0
70, 0.0
80, 0.001
90, 0.0
100, 0.0
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXtend.txt, last written 2026-09-11 00:13 (0.9 days ago) -->
```
3, 0.002
5, 0.001
10, 0.001
20, 0.001
30, 0.0
40, 0.002
50, 0.001
1000, 0.014
5000, 0.026
10000, 0.038
50000, 0.21
100000, 0.407
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXtend.txt, last written 2026-09-11 00:14 (0.9 days ago) -->
```
3, 0.004
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.001
60, 0.0
70, 0.001
80, 0.0
90, 0.0
100, 0.0
1000, 0.002
5000, 0.009
10000, 0.018
50000, 0.053
100000, 0.072
```

