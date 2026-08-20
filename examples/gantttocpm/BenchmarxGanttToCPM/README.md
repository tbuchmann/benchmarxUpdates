# BenchmarxGanttToCPM — Test Suite Documentation

## Overview

This project is a **Benchmarx** benchmark for bidirectional transformations (BX) between two project-management metamodels:

| Side | Model type | Root class | Key elements |
|------|-----------|------------|--------------|
| **Source** | Gantt diagram | `GanttDiagram` | `Activity` (name, duration), `Dependency` (predecessor, successor, `DependencyType`, offset) |
| **Target** | CPM network | `CPMNetwork` | `Event` (number), `Activity` (sourceEvent, targetEvent, name, duration) |

Both models carry an auxiliary `incrementalID` string attribute that is used by certain tests to trigger incremental synchronisation without changing the "real" domain content.

### Dependency types (Gantt side)
The `DependencyType` enumeration has four values that describe which ends of two activities are linked:

| Literal | Meaning |
|---------|---------|
| `START_START` | Successor may not start before predecessor starts |
| `START_END`   | Successor may not end before predecessor starts |
| `END_START`   | Successor may not start before predecessor ends (classic FS) |
| `END_END`     | Successor may not end before predecessor ends |

### CPM representation
In the CPM network each Gantt *activity* becomes a CPM *activity* connecting two numbered *events*. A Gantt *dependency* between two activities is represented as an additional CPM *activity* whose name encodes the relationship (e.g. `"spam tanks->win game"`) and whose source/target events connect the relevant end-events of the two original activities.

---

## Tool implementations under test

The test suite is parameterised and runs against every tool returned by `GanttToCPMTestCase.tools()`. Currently active:

| Tool class | Display name |
|-----------|--------------|
| `BXLangGantt2Cpm` | `BXLang` |
| `BXAgentGantt2Cpm` | *(BXAgent)* |

Commented-out (available but currently not run):
- `BXtendGantt2CPM`
- `MediniQVTGantt2CPM`

---

## Test-suite structure

```
testsuite/
├── batch/
│   ├── fwd/BatchForward.java      – batch forward (Gantt → CPM)
│   └── bwd/BatchBackward.java     – batch backward (CPM → Gantt)
└── alignment_based/
    ├── fwd/IncrementalForward.java – incremental forward
    └── bwd/IncrementalBackward.java– incremental backward
```

### Expected model states (XMI fixtures)

All expected pre- and post-conditions are stored as XMI files in `resources/` and are referenced by name in the tests.

---

## Batch Forward tests (`BatchForward`)

These tests propagate a **complete source (Gantt) model** to produce a fresh target (CPM) model (batch/fwd direction).

### `testInitialiseSynchronisation`
**What is tested:** The agreed-upon starting state immediately after the synchronisation dialogue is initiated, without any explicit source edit.  
**Precondition:** None (initial empty state).  
**Operation:** No source edit is performed.  
**Expected postcondition:**  
- `RootElementGantt` – a `GanttDiagram` root element exists (name not yet set).  
- `RootElementCpm` – a corresponding `CPMNetwork` root element exists.

---

### `testGanttNameChangeOfEmpty`
**What is tested:** Renaming an empty Gantt diagram propagates the new name to the CPM network.  
**Precondition (set up by source edit → asserted):**  
- Source edit: sets the diagram name to `"Gantt2CPM"` (no activities, no dependencies).  
- Assert precondition: `EmptyGantt2CpmGantt` / `EmptyGantt2CpmCpm` – both models exist with name `"Gantt2CPM"` and are empty.  
**Operation (source edit):** Sets the diagram name to `"ItalyTankRush"` (still no activities).  
**Expected postcondition:**  
- `EmptyItalyTankRushGantt` – `GanttDiagram` with name `"ItalyTankRush"`, no activities.  
- `EmptyItalyTankRushCpm` – `CPMNetwork` with name `"ItalyTankRush"`, no elements.

---

### `testCreateGantt`
**What is tested:** Creation of a simple Gantt diagram with two activities and one dependency produces the correct CPM network.  
**Precondition:** None.  
**Operation (source edit — `createSimpleTankRush`):**  
Creates the Gantt diagram `"ItalyTankRush"` with:
- Activity `"spam tanks"` (duration 8)
- Activity `"win game"` (duration 1)
- Dependency `END_START` from `"spam tanks"` to `"win game"` with offset 180  

**Expected postcondition:**  
- `SimpleTankRushGantt` – the Gantt diagram as described above.  
- `SimpleTankRushCpm` – CPM network `"ItalyTankRush"` with:
  - 4 events (numbered 1–4)
  - Activity `"spam tanks"` connecting events 1→2, duration 8
  - Activity `"win game"` connecting events 3→4, duration 1
  - Activity `"spam tanks->win game"` connecting events 2→3, duration 180

---

### `testCreateComplexGantt`
**What is tested:** Creation of a Gantt diagram that exercises all four dependency types produces the correct, more complex CPM network.  
**Precondition:** None.  
**Operation (source edit — `createComplexTankRush`):**  
Creates the Gantt diagram `"ItalyTankRush"` with:
- Activity `"build tankbase"` (duration 5)
- Activity `"research m15"` (duration 75)
- Activity `"spam tanks"` (duration 8)
- Activity `"win game"` (duration 1)
- Dependency `START_START` from `"build tankbase"` to `"research m15"`, offset 6
- Dependency `START_END` from `"research m15"` to `"spam tanks"`, offset 84
- Dependency `END_START` from `"spam tanks"` to `"win game"`, offset 180
- Dependency `END_END` from `"spam tanks"` to `"win game"`, offset 181

**Expected postcondition:**  
- `ComplexTankRushGantt` – the Gantt diagram as described above.  
- `ComplexTankRushCpm` – CPM network `"ItalyTankRush"` with 8 events and 8 activities (4 task activities + 4 dependency activities connecting the appropriate end-events with the encoded offsets as durations).

---

## Batch Backward tests (`BatchBackward`)

These tests propagate a **complete target (CPM) model** to produce a fresh source (Gantt) model (batch/bwd direction).

### `testCpmNameChangeOfEmpty`
**What is tested:** Renaming an empty CPM network propagates the new name back to the Gantt diagram.  
**Precondition (set up by target edit → asserted):**  
- Target edit: sets the network name to `"Gantt2CPM"` (no elements).  
- Assert precondition: `EmptyGantt2CpmGantt` / `EmptyGantt2CpmCpm`.  
**Operation (target edit):** Sets the network name to `"ItalyTankRush"`.  
**Expected postcondition:** `EmptyItalyTankRushGantt` / `EmptyItalyTankRushCpm` (same fixtures as the forward counterpart).

---

### `testCreateCpm`
**What is tested:** Creating a simple CPM network from scratch produces the correct Gantt diagram.  
**Precondition:** None.  
**Operation (target edit — `createSimpleTankRush` on the CPM side):**  
Creates CPM network `"ItalyTankRush"` with 4 events and the activities `"spam tanks"` (1→2, dur 8), `"win game"` (3→4, dur 1), `"spam tanks->win game"` (2→3, dur 180).  
**Expected postcondition:** `SimpleTankRushGantt` / `SimpleTankRushCpm` (identical fixtures to `BatchForward.testCreateGantt`).

---

### `testCreateComplexGantt` (BatchBackward)
**What is tested:** Creating a complex CPM network with all four dependency-type activity variants produces the correct Gantt diagram.  
**Precondition:** None.  
**Operation (target edit — `createComplexTankRush` on the CPM side):**  
Creates CPM network `"ItalyTankRush"` with 8 events and 8 activities encoding all four dependency types.  
**Expected postcondition:** `ComplexTankRushGantt` / `ComplexTankRushCpm` (identical fixtures to `BatchForward.testCreateComplexGantt`).

---

## Incremental Forward tests (`IncrementalForward`)

These tests use an **existing, synchronised model pair** as a precondition, then apply a delta to the Gantt diagram and verify the CPM network is updated correctly.

The scenario domain is a software-project planning model of the Gantt2CPM transformation project itself (activities named after project artefacts: `Gantt2CPMTestCases`, `GanttHelper`, `CPMHelper`, `GanttComparator`, `CPMComparator`, `GanttModel`, `CPMModel`).

---

### `testIncrementalInserts`
**What is tested:** Adding new activities and dependencies to an existing Gantt diagram inserts corresponding events and activities into the CPM network, while preserving all pre-existing elements.  
**Precondition (source edit + idle target edit):**  
- Source: Creates diagram `"Gantt2CPM"` with activity `"Gantt2CPMTestCases"` (dur 5); sets `incrementalID`.  
- Target: Sets `incrementalID` (idle, no structural change).  
- Assert precondition: `TestsGantt` / `TestsCPM`.  
**Operation (source edit):**  
1. `addGantt2CPMHelpers`: Adds activities `"GanttHelper"` (dur 2) and `"CPMHelper"` (dur 2), plus dependencies `END_START(Gantt2CPMTestCases → CPMHelper, 4)` and `START_START(Gantt2CPMTestCases → GanttHelper, 0)`.  
2. `addGantt2CPMComparators`: Adds activities `"GanttComparator"` (dur 3) and `"CPMComparator"` (dur 1), plus `END_END` dependencies from `Gantt2CPMTestCases` to each comparator.  
3. `addGantt2CPMModels`: Adds activities `"GanttModel"` (dur 1) and `"CPMModel"` (dur 1), plus `END_START` dependencies from each model to `Gantt2CPMTestCases`.  
**Expected postcondition:** `TestsHelperModelComparatorGantt` / `TestsHelperModelComparatorCPM` – all seven activities plus all six dependencies are present in both models.

---

### `testIncrementalDeletions`
**What is tested:** Deleting dependencies and then activities from the Gantt diagram removes the corresponding elements from the CPM network.  
**Precondition:** Full "TestsHelperModel-Comparator" state (all seven activities and all six dependencies, including the two model→comparator dependencies).  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.

**Step 1 — Delete dependencies (source edit — `deleteGantt2CPMModelsToComparatorDependencies`):**  
Removes the dependencies from `"GanttModel"` → `"GanttComparator"` and from `"CPMModel"` → `"CPMComparator"`.  
Expected postcondition: `TestsHelperModelComparatorGantt` / `TestsHelperModelComparatorCPM` – seven activities remain; the two model→comparator dependencies are gone.

**Step 2 — Delete activities (source edit — `deleteGantt2CPMHelpers`):**  
Removes activities `"GanttHelper"` and `"CPMHelper"` together with all their incoming and outgoing dependencies.  
Expected postcondition: `TestsModelComparatorGantt` / `TestsModelComparatorCPM` – five activities remain (`Gantt2CPMTestCases`, `GanttComparator`, `CPMComparator`, `GanttModel`, `CPMModel`).

---

### `testIncrementalValueChange`
**What is tested:** Modifying attribute values (name, duration, dependency type, offset, predecessor/successor references) in the Gantt diagram propagates all changes correctly to the CPM network.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.

**Step 1 (source edit):**  
- `changeGantt2CPMHelperToBuilder`: Renames `"GanttHelper"` → `"CPMBuilder"` and `"CPMHelper"` → `"GanttBuilder"`.  
- `changeGantt2CPMModelDuration`: Sets `"GanttModel".duration = 0`, `"CPMModel".duration = 4`.  
Expected postcondition: `TestsBuilderMModel-ComparatorGantt` / `TestsBuilderMModel-ComparatorCPM`.

**Step 2 (source edit):**  
- `changeGantt2CPMTestCasesNameDuration`: Renames `"Gantt2CPMTestCases"` → `"Tests"`, sets duration to 4, and updates offsets of its outgoing comparator dependencies to 1.  
- `changeGantt2CPMModelToComparatorDependencyTypeDurationTargetAndSource`: Modifies the dependency from `"GanttModel"` → `"GanttComparator"`: changes type to `START_START`, re-routes predecessor to `"CPMModel"` and successor to `"CPMBuilder"`, sets offset to 8.  
Expected postcondition: `TestsBuilderModelComparatorModifiedGantt` / `TestsBuilderModelComparatorModifiedCPM`.

---

### `testStability`
**What is tested:** After a synchronised state is reached, performing an idle source edit (no structural or attribute change to domain content) does not alter the target model.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.  
**Operation (source edit — `idleDelta`):** No-op.  
**Expected postcondition:** `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM` (unchanged).

---

### `testHippocraticness`
**What is tested:** Changing only the `incrementalID` (a non-domain attribute) on the source model does not cause any domain changes in the target model.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.  
**Operation (source edit — `changeIncrementalID`):** Toggles the `incrementalID` of the Gantt diagram between `"changed"` and `"changed again"`.  
**Expected postcondition:**  
- `TestsHelperModel-ComparatorChangedAgainGantt` – same domain content, updated `incrementalID`.  
- `TestsHelperModel-ComparatorCPM` – CPM network is **unchanged**.

---

## Incremental Backward tests (`IncrementalBackward`)

Mirror of the incremental forward suite, but the delta is applied to the **CPM network** and propagated back to the Gantt diagram.

---

### `testIncrementalInserts` (backward)
**What is tested:** Adding new events and activities to an existing CPM network inserts corresponding activities and dependencies into the Gantt diagram, while preserving all pre-existing elements.  
**Precondition:** Target edit creates a CPM network `"Gantt2CPM"` with activity `"Gantt2CPMTestCases"` (events 1–2, dur 5); `incrementalID` is set on both sides.  
Assert precondition: `TestsGantt` / `TestsCPM`.  
**Operation (target edit):**  
1. `addCPM2GanttHelpers`: Adds events 3–6 and activities `"GanttHelper"` (3→4, dur 2), `"CPMHelper"` (5→6, dur 2), `"Gantt2CPMTestCases->CPMHelper"` (1→6, dur 4), `"Gantt2CPMTestCases->GanttHelper"` (1→3, dur 0).  
2. `addCPM2GanttComparators`: Adds events 7–10 and activities `"GanttComparator"` (7→8, dur 3), `"CPMComparator"` (9→10, dur 1), `"Gantt2CPMTestCases->GanttComparator"` (2→8, dur 0), `"Gantt2CPMTestCases->CPMComparator"` (2→10, dur 0).  
3. `addCPM2GanttModels`: Adds events 11–14 and activities `"GanttModel"` (11→12, dur 1), `"CPMModel"` (13→14, dur 1), `"GanttModel->Gantt2CPMTestCases"` (12→1, dur 1), `"CPMModel->Gantt2CPMTestCases"` (14→1, dur 2).  
**Expected postcondition:** `TestsHelperModelComparatorGantt` / `TestsHelperModelComparatorCPM`.

---

### `testIncrementalDeletions` (backward)
**What is tested:** Deleting activities from the CPM network removes the corresponding activities and dependencies from the Gantt diagram.  
**Precondition:** Full "TestsHelperModel-Comparator" state (including the two model→comparator dependency activities).  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.

**Step 1 — Delete dependency activities (target edit — `deleteCPM2GanttModelsToComparatorDependencies`):**  
Removes activities `"GanttModel->GanttComparator"` and `"CPMModel->CPMComparator"` from the CPM network.  
Expected postcondition: `TestsHelperModelComparatorGantt` / `TestsHelperModelComparatorCPM`.

**Step 2 — Delete helper activities (target edit — `deleteCPM2GanttHelpers`):**  
Removes activities `"GanttHelper"`, `"CPMHelper"`, `"Gantt2CPMTestCases->CPMHelper"`, `"Gantt2CPMTestCases->GanttHelper"`, and the now-dangling events 3–6.  
Expected postcondition: `TestsModelComparatorGantt` / `TestsModelComparatorCPM`.

---

### `testIncrementalValueChange` (backward)
**What is tested:** Modifying attribute values (name, duration, source/target event references) in the CPM network propagates all changes correctly to the Gantt diagram.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.

**Step 1 (target edit):**  
- `changeCPM2GanttHelperToBuilder`: Renames `"GanttHelper"` → `"CPMBuilder"`, `"CPMHelper"` → `"GanttBuilder"`, and the two connecting activities accordingly.  
- `changeCPM2GanttModelDuration`: Sets `"GanttModel".duration = 0`, `"CPMModel".duration = 4`.  
Expected postcondition: `TestsBuilderMModel-ComparatorGantt` / `TestsBuilderMModel-ComparatorCPM`.

**Step 2 (target edit):**  
- `changeCPM2GanttTestCasesNameDuration`: Renames `"Gantt2CPMTestCases"` → `"Tests"` in both the main activity and all connecting activities (e.g. `"Gantt2CPMTestCases->CPMComparator"` → `"Tests->CPMComparator"`); sets duration to 4; sets durations of `"Tests->CPMComparator"` and `"Tests->GanttComparator"` to 1.  
- `changeCPM2GanttModelToComparatorDependencyTypeDurationTargetAndSource`: Renames `"GanttModel->GanttComparator"` to `"CPMModel->CPMBuilder"`, re-routes its source event to event 13 and target event to event 3, and sets its duration to 8.  
Expected postcondition: `TestsBuilderModelComparatorModifiedGantt` / `TestsBuilderModelComparatorModifiedCPM`.

---

### `testStability` (backward)
**What is tested:** After a synchronised state is reached, an idle target edit (no-op) does not alter the source model.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.  
**Operation (target edit — `idleDelta`):** No-op.  
**Expected postcondition:** `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM` (unchanged).

---

### `testHipporcraticness` (backward)
**What is tested:** Changing only the `incrementalID` on the CPM network does not cause any domain changes in the Gantt diagram.  
**Precondition:** Full "TestsHelperModel-Comparator" state.  
Assert precondition: `TestsHelperModel-ComparatorGantt` / `TestsHelperModel-ComparatorCPM`.  
**Operation (target edit — `changeIncrementalID`):** Toggles the CPM network's `incrementalID` between `"changed"` and `"changed again"`.  
**Expected postcondition:**  
- `TestsHelperModel-ComparatorGantt` – Gantt diagram is **unchanged**.  
- `TestsHelperModel-ComparatorChangedAgainCPM` – CPM network has updated `incrementalID`, domain content unchanged.

---

## Summary table

| Class | Test method | Direction | Category | Key BX property |
|-------|-------------|-----------|----------|-----------------|
| `BatchForward` | `testInitialiseSynchronisation` | fwd | batch | initialisation |
| `BatchForward` | `testGanttNameChangeOfEmpty` | fwd | batch | attribute change |
| `BatchForward` | `testCreateGantt` | fwd | batch | creation |
| `BatchForward` | `testCreateComplexGantt` | fwd | batch | creation, all dependency types |
| `BatchBackward` | `testCpmNameChangeOfEmpty` | bwd | batch | attribute change |
| `BatchBackward` | `testCreateCpm` | bwd | batch | creation |
| `BatchBackward` | `testCreateComplexGantt` | bwd | batch | creation, all dependency types |
| `IncrementalForward` | `testIncrementalInserts` | fwd | incremental | add elements |
| `IncrementalForward` | `testIncrementalDeletions` | fwd | incremental | delete elements |
| `IncrementalForward` | `testIncrementalValueChange` | fwd | incremental | attribute & structural change |
| `IncrementalForward` | `testStability` | fwd | incremental | stability |
| `IncrementalForward` | `testHippocraticness` | fwd | incremental | hippocraticness |
| `IncrementalBackward` | `testIncrementalInserts` | bwd | incremental | add elements |
| `IncrementalBackward` | `testIncrementalDeletions` | bwd | incremental | delete elements |
| `IncrementalBackward` | `testIncrementalValueChange` | bwd | incremental | attribute & structural change |
| `IncrementalBackward` | `testStability` | bwd | incremental | stability |
| `IncrementalBackward` | `testHipporcraticness` | bwd | incremental | hippocraticness |
