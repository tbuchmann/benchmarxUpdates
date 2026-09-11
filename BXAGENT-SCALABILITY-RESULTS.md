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

## Raw data (all measured points, per example)

Every line below is copied verbatim from the corresponding `scalability_results/<label>_<tool>.txt` file (`size, seconds` per line, mean of `REPEAT=5` runs) — nothing is filtered or downsampled. Use this section as the data source for scatterplots; the tables above are only a condensed summary.


### AST → DAG (`asttodag`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:15 (0.9 days ago) -->
```
3, 0.002
5, 0.001
10, 0.001
50, 0.004
100, 0.004
300, 0.008
1000, 0.031
5000, 0.109
10000, 0.152
50000, 1.188
100000, 3.047
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:15 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.002
1000, 0.021
5000, 0.056
10000, 0.076
50000, 0.392
100000, 0.774
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:15 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.002
50, 0.004
100, 0.004
300, 0.004
1000, 0.011
5000, 0.082
10000, 0.124
50000, 0.451
100000, 0.893
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:15 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.002
50, 0.007
100, 0.003
1000, 0.013
5000, 0.062
10000, 0.103
50000, 0.35
100000, 0.71
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.006
5, 0.003
10, 0.003
20, 0.003
30, 0.003
40, 0.003
50, 0.003
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.007
5, 0.004
10, 0.005
20, 0.004
30, 0.005
40, 0.004
50, 0.003
60, 0.003
70, 0.003
80, 0.004
90, 0.003
100, 0.002
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.002
20, 0.003
30, 0.004
40, 0.003
50, 0.003
1000, 0.033
5000, 0.167
10000, 0.195
50000, 0.875
100000, 1.728
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.004
5, 0.002
10, 0.002
20, 0.003
30, 0.003
40, 0.003
50, 0.003
60, 0.003
70, 0.002
80, 0.004
90, 0.003
100, 0.003
1000, 0.021
5000, 0.119
10000, 0.198
50000, 0.673
100000, 1.264
```


### Bag1 → Bag2 (`bag1tobag2`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.001
5, 0.0
10, 0.001
50, 0.001
100, 0.001
300, 0.002
1000, 0.008
5000, 0.03
10000, 0.03
50000, 0.172
100000, 0.32
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.002
1000, 0.008
5000, 0.031
10000, 0.034
50000, 0.144
100000, 0.263
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:16 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.001
100, 0.002
300, 0.002
1000, 0.004
5000, 0.021
10000, 0.036
50000, 0.126
100000, 0.158
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.002
1000, 0.005
5000, 0.011
10000, 0.02
50000, 0.093
100000, 0.1
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.001
20, 0.002
30, 0.001
40, 0.001
50, 0.001
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.002
20, 0.002
30, 0.002
40, 0.002
50, 0.002
60, 0.002
70, 0.002
80, 0.002
90, 0.002
100, 0.002
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
20, 0.001
30, 0.001
40, 0.002
50, 0.001
1000, 0.014
5000, 0.031
10000, 0.061
50000, 0.205
100000, 0.277
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.002
20, 0.001
30, 0.002
40, 0.002
50, 0.002
60, 0.001
70, 0.001
80, 0.002
90, 0.001
100, 0.001
1000, 0.007
5000, 0.03
10000, 0.056
50000, 0.197
100000, 0.29
```


### Ecore → SQL (`ecoretosql`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:17 (0.9 days ago) -->
```
3, 0.001
5, 0.002
10, 0.002
50, 0.004
100, 0.005
300, 0.013
1000, 0.033
5000, 0.12
10000, 0.162
50000, 0.907
100000, 1.728
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:18 (0.9 days ago) -->
```
3, 0.002
5, 0.002
10, 0.002
50, 0.004
100, 0.006
1000, 0.043
5000, 0.118
10000, 0.26
50000, 1.472
100000, 2.748
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:18 (0.9 days ago) -->
```
3, 0.002
5, 0.002
10, 0.004
50, 0.005
100, 0.008
300, 0.016
1000, 0.027
5000, 0.103
10000, 0.182
50000, 0.742
100000, 1.728
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:19 (0.9 days ago) -->
```
3, 0.004
5, 0.001
10, 0.002
50, 0.005
100, 0.009
1000, 0.041
5000, 0.165
10000, 0.24
50000, 1.373
100000, 2.557
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:19 (0.9 days ago) -->
```
3, 0.008
5, 0.007
10, 0.006
20, 0.005
30, 0.005
40, 0.005
50, 0.005
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:19 (0.9 days ago) -->
```
3, 0.013
5, 0.007
10, 0.008
20, 0.007
30, 0.006
40, 0.006
50, 0.005
60, 0.005
70, 0.005
80, 0.005
90, 0.005
100, 0.005
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXAgent.txt, last written 2026-09-11 00:19 (0.9 days ago) -->
```
3, 0.003
5, 0.003
10, 0.004
20, 0.007
30, 0.005
40, 0.006
50, 0.007
1000, 0.075
5000, 0.277
10000, 0.37
50000, 2.076
100000, 3.836
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:19 (0.9 days ago) -->
```
3, 0.006
5, 0.003
10, 0.006
20, 0.006
30, 0.009
40, 0.006
50, 0.005
60, 0.006
70, 0.006
80, 0.006
90, 0.009
100, 0.008
1000, 0.062
5000, 0.286
10000, 0.361
50000, 2.031
100000, 4.036
```


### Families → Persons (`familiestopersons`)


#### Batch Forward (FWD)

<!-- source: FWD_BXagent.txt, last written 2026-09-11 00:20 (0.9 days ago) -->
```
3, 0.002
5, 0.001
10, 0.002
50, 0.003
100, 0.004
300, 0.009
1000, 0.025
5000, 0.11
10000, 0.104
50000, 0.671
100000, 1.315
```


#### Batch Backward (BWD)

<!-- source: BWD_BXagent.txt, last written 2026-09-11 00:20 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.002
50, 0.003
100, 0.004
1000, 0.032
5000, 0.085
10000, 0.111
50000, 0.663
100000, 1.362
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXagent.txt, last written 2026-09-11 00:20 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.005
300, 0.008
1000, 0.017
5000, 0.078
10000, 0.14
50000, 0.427
100000, 1.085
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXagent.txt, last written 2026-09-11 00:20 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.003
50, 0.003
100, 0.006
300, 0.012
1000, 0.028
5000, 0.11
10000, 0.197
50000, 0.856
100000, 1.627
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXagent.txt, last written 2026-09-11 00:21 (0.9 days ago) -->
```
3, 0.006
5, 0.005
10, 0.006
20, 0.008
30, 0.009
40, 0.01
50, 0.012
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXagent.txt, last written 2026-09-11 00:21 (0.9 days ago) -->
```
3, 0.008
5, 0.007
10, 0.008
20, 0.009
30, 0.01
40, 0.012
50, 0.013
60, 0.015
70, 0.015
80, 0.017
90, 0.019
100, 0.021
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXagent.txt, last written 2026-09-11 00:21 (0.9 days ago) -->
```
3, 0.004
5, 0.003
10, 0.003
20, 0.004
30, 0.005
40, 0.006
50, 0.009
1000, 0.122
5000, 0.435
10000, 0.719
50000, 3.91
100000, 6.914
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXagent.txt, last written 2026-09-11 00:21 (0.9 days ago) -->
```
3, 0.006
5, 0.003
10, 0.004
20, 0.003
30, 0.004
40, 0.004
50, 0.008
60, 0.007
70, 0.005
80, 0.005
90, 0.006
100, 0.008
1000, 0.071
5000, 0.304
10000, 0.442
50000, 2.171
100000, 3.414
```


### Gantt → CPM (`gantttocpm`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:22 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.003
300, 0.004
1000, 0.009
5000, 0.066
10000, 0.161
50000, 1.012
100000, 3.304
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:22 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.002
1000, 0.01
5000, 0.038
10000, 0.045
50000, 0.258
100000, 0.385
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:22 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.001
100, 0.002
300, 0.003
1000, 0.004
5000, 0.015
10000, 0.026
50000, 0.106
100000, 0.14
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:23 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.0
50, 0.001
100, 0.001
1000, 0.01
5000, 0.021
10000, 0.026
50000, 0.138
100000, 0.176
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:23 (0.9 days ago) -->
```
3, 0.046
5, 0.043
10, 0.039
20, 0.033
30, 0.037
40, 0.042
50, 0.033
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.048
5, 0.045
10, 0.045
20, 0.042
30, 0.048
40, 0.059
50, 0.061
60, 0.067
70, 0.075
80, 0.081
90, 0.089
100, 0.102
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_BXAgent.txt, last written 2026-09-11 00:23 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.0
20, 0.001
30, 0.002
40, 0.002
50, 0.002
1000, 0.013
5000, 0.044
10000, 0.073
50000, 0.309
100000, 0.544
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:23 (0.9 days ago) -->
```
3, 0.003
5, 0.001
10, 0.001
20, 0.002
30, 0.002
40, 0.002
50, 0.001
60, 0.003
70, 0.001
80, 0.001
90, 0.001
100, 0.002
1000, 0.013
5000, 0.049
10000, 0.066
50000, 0.335
100000, 0.437
```


### PDB1 → PDB2 (`pdb1topdb2`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.002
300, 0.003
1000, 0.008
5000, 0.026
10000, 0.043
50000, 0.211
100000, 0.403
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.002
1000, 0.009
5000, 0.029
10000, 0.041
50000, 0.223
100000, 0.403
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.0
50, 0.002
100, 0.002
300, 0.003
1000, 0.004
5000, 0.015
10000, 0.022
50000, 0.098
100000, 0.181
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.0
5, 0.0
10, 0.0
50, 0.001
100, 0.001
300, 0.003
1000, 0.004
5000, 0.014
10000, 0.026
50000, 0.089
100000, 0.177
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:25 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.002
20, 0.002
30, 0.002
40, 0.001
50, 0.001
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:25 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.002
20, 0.002
30, 0.002
40, 0.002
50, 0.002
60, 0.003
70, 0.002
80, 0.002
90, 0.002
100, 0.003
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
20, 0.001
30, 0.001
40, 0.002
50, 0.001
1000, 0.011
5000, 0.03
10000, 0.05
50000, 0.224
100000, 0.289
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:24 (0.9 days ago) -->
```
3, 0.003
5, 0.002
10, 0.001
20, 0.002
30, 0.002
40, 0.001
50, 0.001
60, 0.002
70, 0.001
80, 0.001
90, 0.001
100, 0.001
1000, 0.007
5000, 0.034
10000, 0.048
50000, 0.218
100000, 0.335
```


### Petrinet → PetrinetWeighted (`pntopnw`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:25 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.003
100, 0.003
300, 0.007
1000, 0.019
5000, 0.059
10000, 0.091
50000, 0.538
100000, 1.128
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:25 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.004
1000, 0.023
5000, 0.066
10000, 0.091
50000, 0.49
100000, 1.089
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:25 (0.9 days ago) -->
```
3, 0.001
5, 0.002
10, 0.001
50, 0.003
100, 0.003
300, 0.004
1000, 0.013
5000, 0.058
10000, 0.089
50000, 0.418
100000, 0.681
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:26 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.002
50, 0.002
100, 0.003
300, 0.007
1000, 0.014
5000, 0.062
10000, 0.094
50000, 0.323
100000, 0.64
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:26 (0.9 days ago) -->
```
3, 0.004
5, 0.003
10, 0.003
20, 0.004
30, 0.003
40, 0.003
50, 0.002
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:26 (0.9 days ago) -->
```
3, 0.005
5, 0.004
10, 0.003
20, 0.004
30, 0.003
40, 0.003
50, 0.003
60, 0.003
70, 0.003
80, 0.004
90, 0.003
100, 0.003
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXAgent.txt, last written 2026-09-11 00:26 (0.9 days ago) -->
```
3, 0.006
5, 0.001
10, 0.002
20, 0.002
30, 0.003
40, 0.004
50, 0.004
1000, 0.029
5000, 0.137
10000, 0.199
50000, 1.062
100000, 1.529
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:26 (0.9 days ago) -->
```
3, 0.004
5, 0.002
10, 0.002
20, 0.003
30, 0.003
40, 0.003
50, 0.003
60, 0.003
70, 0.003
80, 0.003
90, 0.003
100, 0.003
1000, 0.04
5000, 0.129
10000, 0.209
50000, 1.081
100000, 1.597
```


### Set → OSet (`settooset`)


#### Batch Forward (FWD)

<!-- source: FWD_BXAgent.txt, last written 2026-09-11 00:27 (0.9 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.002
100, 0.002
300, 0.005
1000, 0.013
5000, 0.101
10000, 0.225
50000, 1.608
100000, 5.547
```


#### Batch Backward (BWD)

<!-- source: BWD_BXAgent.txt, last written 2026-09-11 00:27 (0.9 days ago) -->
```
3, 0.0
5, 0.001
10, 0.001
50, 0.003
100, 0.001
1000, 0.009
5000, 0.036
10000, 0.04
50000, 0.147
100000, 0.287
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_BXAgent.txt, last written 2026-09-11 00:28 (0.8 days ago) -->
```
3, 0.001
5, 0.0
10, 0.001
50, 0.001
100, 0.002
300, 0.004
1000, 0.008
5000, 0.016
10000, 0.033
50000, 0.108
100000, 0.189
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_BXAgent.txt, last written 2026-09-11 00:28 (0.8 days ago) -->
```
3, 0.001
5, 0.001
10, 0.001
50, 0.001
100, 0.002
300, 0.003
1000, 0.005
5000, 0.015
10000, 0.028
50000, 0.089
100000, 0.195
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_BXAgent.txt, last written 2026-09-11 00:28 (0.8 days ago) -->
```
3, 0.002
5, 0.001
10, 0.002
20, 0.002
30, 0.002
40, 0.002
50, 0.003
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_BXAgent.txt, last written 2026-09-11 00:29 (0.8 days ago) -->
```
3, 0.002
5, 0.001
10, 0.001
20, 0.001
30, 0.001
40, 0.001
50, 0.001
60, 0.001
70, 0.001
80, 0.001
90, 0.001
100, 0.001
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_BXAgent.txt, last written 2026-09-11 00:28 (0.8 days ago) -->
```
3, 0.002
5, 0.002
10, 0.002
20, 0.002
30, 0.001
40, 0.002
50, 0.001
1000, 0.013
5000, 0.039
10000, 0.075
50000, 0.312
100000, 0.48
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_BXAgent.txt, last written 2026-09-11 00:28 (0.8 days ago) -->
```
3, 0.002
5, 0.001
10, 0.002
20, 0.001
30, 0.001
40, 0.002
50, 0.002
60, 0.002
70, 0.002
80, 0.001
90, 0.001
100, 0.001
1000, 0.008
5000, 0.032
10000, 0.06
50000, 0.215
100000, 0.336
```

