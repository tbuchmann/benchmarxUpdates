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

## Raw data (all measured points, per example)

Every line below is copied verbatim from the corresponding `scalability_results/<label>_<tool>.txt` file (`size, seconds` per line, mean of `REPEAT=5` runs) — nothing is filtered or downsampled. Use this section as the data source for scatterplots; the tables above are only a condensed summary.


### AST → DAG (`asttodag`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 00:31 (0.8 days ago) -->
```
3, 0.016
5, 0.013
10, 0.015
50, 0.036
100, 0.06
300, 0.145
1000, 1.029
5000, 22.219
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 00:33 (0.8 days ago) -->
```
3, 0.011
5, 0.008
10, 0.006
50, 0.005
100, 0.005
1000, 0.005
5000, 0.005
10000, 0.004
50000, 0.004
100000, 0.003
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 00:35 (0.8 days ago) -->
```
3, 0.017
5, 0.011
10, 0.009
50, 0.017
100, 0.031
300, 0.03
1000, 0.112
5000, 0.154
10000, 0.279
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 00:38 (0.8 days ago) -->
```
3, 0.012
5, 0.011
10, 0.007
50, 0.007
100, 0.007
1000, 0.005
5000, 0.006
10000, 0.005
50000, 0.005
100000, 0.004
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 00:48 (0.8 days ago) -->
```
3, 0.073
5, 0.045
10, 0.034
20, 0.031
30, 0.031
40, 0.029
50, 0.029
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 00:48 (0.8 days ago) -->
```
3, 0.027
5, 0.019
10, 0.011
20, 0.01
30, 0.01
40, 0.009
50, 0.008
60, 0.009
70, 0.007
80, 0.008
90, 0.008
100, 0.008
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCSync_MediniQVT.txt, last written 2026-09-11 00:42 (0.8 days ago) -->
```
3, 0.05
5, 0.029
10, 0.029
20, 0.034
30, 0.037
40, 0.04
50, 0.045
1000, 1.443
5000, 25.73
10000, 116.635
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 00:45 (0.8 days ago) -->
```
3, 0.036
5, 0.025
10, 0.017
20, 0.025
30, 0.019
40, 0.017
50, 0.016
60, 0.018
70, 0.016
80, 0.022
90, 0.024
100, 0.018
1000, 0.056
5000, 0.178
10000, 0.326
```


### Bag1 → Bag2 (`bag1tobag2`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 00:50 (0.8 days ago) -->
```
3, 0.008
5, 0.009
10, 0.007
50, 0.044
100, 0.315
300, 20.886
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 00:52 (0.8 days ago) -->
```
3, 0.007
5, 0.004
10, 0.003
50, 0.003
100, 0.003
1000, 0.004
5000, 0.003
10000, 0.003
50000, 0.003
100000, 0.002
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 00:53 (0.8 days ago) -->
```
3, 0.02
5, 0.016
10, 0.019
50, 0.07
100, 0.349
300, 20.794
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 00:57 (0.8 days ago) -->
```
3, 0.007
5, 0.006
10, 0.005
50, 0.004
100, 0.003
300, 0.004
1000, 0.004
5000, 0.003
10000, 0.002
50000, 0.002
100000, 0.002
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 01:08 (0.8 days ago) -->
```
3, 0.223
5, 0.197
10, 0.165
20, 0.106
30, 0.066
40, 0.041
50, 0.024
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 01:08 (0.8 days ago) -->
```
3, 0.27
5, 0.235
10, 0.24
20, 0.239
30, 0.251
40, 0.263
50, 0.272
60, 0.292
70, 0.319
80, 0.356
90, 0.408
100, 0.486
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_MediniQVT.txt, last written 2026-09-11 00:57 (0.8 days ago) -->
```
3, 0.024
5, 0.016
10, 0.028
20, 0.02
30, 0.028
40, 0.03
50, 0.043
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 01:03 (0.8 days ago) -->
```
3, 0.03
5, 0.033
10, 0.021
20, 0.021
30, 0.026
40, 0.035
50, 0.051
60, 0.079
70, 0.108
80, 0.167
90, 0.239
100, 0.322
```


### Ecore → SQL (`ecoretosql`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 01:10 (0.8 days ago) -->
```
3, 0.03
5, 0.024
10, 0.022
50, 0.033
100, 0.05
300, 0.532
1000, 17.268
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 01:14 (0.8 days ago) -->
```
3, 0.032
5, 0.022
10, 0.024
50, 0.033
100, 0.087
1000, 21.914
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 01:17 (0.8 days ago) -->
```
3, 0.046
5, 0.033
10, 0.029
50, 0.039
100, 0.072
300, 0.516
1000, 16.018
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 01:22 (0.8 days ago) -->
```
3, 0.045
5, 0.033
10, 0.04
50, 0.077
100, 0.1
1000, 21.632
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-08-19 01:48 (23.8 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 24 days; not fresh data.

```
3, 0.0
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-08-19 01:48 (23.8 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 24 days; not fresh data.

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

<!-- source: CDCSync_MediniQVT.txt, last written 2026-09-11 01:27 (0.8 days ago) -->
```
3, 0.108
5, 0.132
10, 0.374
20, 5.657
30, 37.476
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 01:30 (0.8 days ago) -->
```
3, 0.114
5, 0.101
10, 0.39
20, 5.893
30, 38.644
```


### Families → Persons (`familiestopersons`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 01:36 (0.8 days ago) -->
```
3, 0.018
5, 0.012
10, 0.014
50, 0.035
100, 0.078
300, 1.232
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 01:39 (0.8 days ago) -->
```
3, 0.018
5, 0.013
10, 0.017
50, 0.273
100, 1.952
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 01:44 (0.8 days ago) -->
```
3, 0.03
5, 0.022
10, 0.042
50, 0.061
100, 0.11
300, 1.276
1000, 41.433
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 01:49 (0.8 days ago) -->
```
3, 0.04
5, 0.03
10, 0.028
50, 0.305
100, 2.043
300, 51.33
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 02:03 (0.8 days ago) -->
```
3, 1.862
5, 1.803
10, 1.811
20, 1.789
30, 1.838
40, 1.84
50, 1.919
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 02:06 (0.8 days ago) -->
```
3, 2.545
5, 2.516
10, 2.583
20, 2.731
30, 2.939
40, 3.16
50, 3.331
60, 3.513
70, 3.701
80, 3.934
90, 4.133
100, 4.315
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_MediniQVT.txt, last written 2026-09-11 01:53 (0.8 days ago) -->
```
3, 0.07
5, 0.06
10, 0.077
20, 0.204
30, 0.452
40, 0.998
50, 1.678
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 01:58 (0.8 days ago) -->
```
3, 0.066
5, 0.039
10, 0.049
20, 0.089
30, 0.146
40, 0.225
50, 0.357
60, 0.552
70, 0.828
80, 1.16
90, 1.604
100, 2.163
```


### Gantt → CPM (`gantttocpm`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 02:06 (0.8 days ago) -->
```
3, 0.02
5, 0.018
10, 0.025
50, 0.17
100, 1.402
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 02:08 (0.8 days ago) -->
```
3, 0.02
5, 0.027
10, 0.143
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 02:12 (0.8 days ago) -->
```
3, 0.038
5, 0.019
10, 0.025
50, 0.056
100, 0.089
300, 1.256
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 02:16 (0.8 days ago) -->
```
3, 0.052
5, 0.046
10, 0.224
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-08-19 00:58 (23.8 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 24 days; not fresh data.

```
3, 0.0
5, 0.0
10, 0.0
20, 0.0
30, 0.0
40, 0.0
50, 0.0
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-08-19 00:58 (23.8 days ago) -->
**⚠️ STALE** — this file predates the 2026-09-11 run by 24 days; not fresh data.

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

<!-- source: CDCSync_MediniQVT.txt, last written 2026-09-11 02:23 (0.8 days ago) -->
```
3, 0.088
5, 0.06
10, 0.273
20, 3.741
30, 24.043
40, 100.14
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 02:26 (0.8 days ago) -->
```
3, 0.141
5, 0.181
10, 0.694
20, 7.329
30, 39.257
```


### PDB1 → PDB2 (`pdb1topdb2`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 02:34 (0.8 days ago) -->
```
3, 0.011
5, 0.008
10, 0.009
50, 0.018
100, 0.019
300, 0.051
1000, 0.197
5000, 4.03
10000, 16.834
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 02:38 (0.8 days ago) -->
```
3, 0.01
5, 0.008
10, 0.008
50, 0.016
100, 0.019
1000, 0.217
5000, 3.98
10000, 16.777
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 02:40 (0.8 days ago) -->
```
3, 0.016
5, 0.014
10, 0.024
50, 0.043
100, 0.03
300, 0.066
1000, 0.259
5000, 3.019
10000, 11.755
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 02:45 (0.8 days ago) -->
```
3, 0.022
5, 0.012
10, 0.016
50, 0.031
100, 0.041
300, 0.066
1000, 0.244
5000, 3.424
10000, 13.708
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 03:00 (0.7 days ago) -->
```
3, 0.045
5, 0.036
10, 0.029
20, 0.02
30, 0.017
40, 0.014
50, 0.013
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 03:00 (0.7 days ago) -->
```
3, 0.045
5, 0.038
10, 0.035
20, 0.026
30, 0.025
40, 0.034
50, 0.029
60, 0.033
70, 0.037
80, 0.042
90, 0.044
100, 0.047
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_MediniQVT.txt, last written 2026-09-11 02:51 (0.8 days ago) -->
```
3, 0.039
5, 0.022
10, 0.022
20, 0.037
30, 0.025
40, 0.028
50, 0.027
1000, 0.459
5000, 6.236
10000, 24.825
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 02:56 (0.7 days ago) -->
```
3, 0.046
5, 0.026
10, 0.024
20, 0.026
30, 0.025
40, 0.027
50, 0.032
60, 0.043
70, 0.032
80, 0.04
90, 0.046
100, 0.048
1000, 0.41
5000, 6.768
10000, 27.795
```


### Petrinet → PetrinetWeighted (`pntopnw`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 03:02 (0.7 days ago) -->
```
3, 0.013
5, 0.01
10, 0.011
50, 0.018
100, 0.03
300, 0.083
1000, 0.678
5000, 17.471
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 03:06 (0.7 days ago) -->
```
3, 0.013
5, 0.008
10, 0.009
50, 0.023
100, 0.029
1000, 0.694
5000, 17.688
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 03:09 (0.7 days ago) -->
```
3, 0.032
5, 0.031
10, 0.029
50, 0.05
100, 0.096
300, 0.273
1000, 1.86
5000, 50.258
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 03:14 (0.7 days ago) -->
```
3, 0.026
5, 0.029
10, 0.032
50, 0.047
100, 0.071
300, 0.287
1000, 1.935
5000, 53.413
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 03:28 (0.7 days ago) -->
```
3, 0.057
5, 0.033
10, 0.034
20, 0.025
30, 0.026
40, 0.026
50, 0.023
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 03:28 (0.7 days ago) -->
```
3, 0.083
5, 0.034
10, 0.034
20, 0.036
30, 0.039
40, 0.044
50, 0.046
60, 0.049
70, 0.053
80, 0.056
90, 0.063
100, 0.068
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_MediniQVT.txt, last written 2026-09-11 03:20 (0.7 days ago) -->
```
3, 0.05
5, 0.032
10, 0.032
20, 0.054
30, 0.047
40, 0.058
50, 0.062
1000, 3.474
5000, 95.748
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 03:25 (0.7 days ago) -->
```
3, 0.07
5, 0.036
10, 0.036
20, 0.052
30, 0.05
40, 0.061
50, 0.098
60, 0.102
70, 0.109
80, 0.109
90, 0.092
100, 0.078
1000, 3.586
5000, 103.244
```


### Set → OSet (`settooset`)


#### Batch Forward (FWD)

<!-- source: FWD_MediniQVT.txt, last written 2026-09-11 03:28 (0.7 days ago) -->
```
3, 0.015
5, 0.011
10, 0.014
50, 0.164
100, 1.658
```


#### Batch Backward (BWD)

<!-- source: BWD_MediniQVT.txt, last written 2026-09-11 03:30 (0.7 days ago) -->
```
3, 0.011
5, 0.011
10, 0.009
50, 0.13
100, 1.633
```


#### Incremental Forward (INCR_FWD)

<!-- source: INCR_FWD_MediniQVT.txt, last written 2026-09-11 03:33 (0.7 days ago) -->
```
3, 0.022
5, 0.019
10, 0.027
50, 0.193
100, 1.701
```


#### Incremental Backward (INCR_BWD)

<!-- source: INCR_BWD_MediniQVT.txt, last written 2026-09-11 03:40 (0.7 days ago) -->
```
3, 0.019
5, 0.021
10, 0.016
50, 0.126
100, 1.46
300, 103.172
```


#### Concurrent, constant model / growing conflicting delta (CMCSync)

<!-- source: CMCSync_MediniQVT.txt, last written 2026-09-11 03:54 (0.7 days ago) -->
```
3, 3.201
5, 3.163
10, 2.558
20, 1.614
30, 0.966
40, 0.537
50, 0.273
```


#### Concurrent, constant model / growing conflict-free delta (CMCFCSync)

<!-- source: CMCFCSync_MediniQVT.txt, last written 2026-09-11 03:58 (0.7 days ago) -->
```
3, 1.734
5, 2.077
10, 2.498
20, 3.559
30, 4.87
40, 6.514
50, 8.59
60, 11.221
```


#### Concurrent, constant delta / growing model, conflicting (CDCSync)

<!-- source: CDCsync_MediniQVT.txt, last written 2026-09-11 03:42 (0.7 days ago) -->
```
3, 0.029
5, 0.022
10, 0.023
20, 0.035
30, 0.059
40, 0.118
50, 0.236
```


#### Concurrent, constant delta / growing model, conflict-free (CDCFCSync)

<!-- source: CDCFCSync_MediniQVT.txt, last written 2026-09-11 03:47 (0.7 days ago) -->
```
3, 0.046
5, 0.03
10, 0.028
20, 0.045
30, 0.065
40, 0.108
50, 0.194
60, 0.307
70, 0.541
80, 0.83
90, 1.243
100, 1.847
```

