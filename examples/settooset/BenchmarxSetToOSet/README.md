# BenchmarxSetToOSet

A [Benchmarx](https://github.com/eMoflon/benchmarx) benchmark for bidirectional transformation (BX) tools.
It synchronises an **unordered set** (`MySet`, source) with an **ordered set** (`MyOrderedSet`, target).

---

## Table of Contents

1. [Overview](#overview)
2. [Metamodels](#metamodels)
3. [Named Model States (Resources)](#named-model-states-resources)
4. [Tool Implementations Under Test](#tool-implementations-under-test)
5. [Test Suite Structure](#test-suite-structure)
6. [Test Classes and Test Cases](#test-classes-and-test-cases)
   - [BatchForward](#1-batchforward)
   - [BatchBackward](#2-batchbackward)
   - [IncrementalBackward](#3-incrementalbackward)
   - [IncrementalForward](#4-incrementalforward)
7. [Helper Operations Reference](#helper-operations-reference)
8. [Running the Tests](#running-the-tests)

---

## Overview

The benchmark evaluates how well a BX tool keeps a plain, unordered `MySet` consistent with an
`MyOrderedSet` that stores the same elements in a doubly-linked list.  Because the target adds
ordering information that has no counterpart in the source, the synchronisation must decide *where*
to insert newly created elements; several valid target states may therefore be acceptable for a
given forward propagation.

The test suite is structured along two axes:

| Axis | Values |
|------|--------|
| **Direction** | Forward (`fwd`): source edits propagated to target · Backward (`bwd`): target edits propagated to source |
| **Mode** | Batch: the tool processes a complete, freshly assembled model · Incremental (alignment-based): the tool receives a sequence of individual deltas |

Every test is run **once per active tool** through JUnit 5 parameterisation.

---

## Metamodels

### Source – `MySet` (package `sets`)

| Feature | Type | Description |
|---------|------|-------------|
| `name` | `String` attribute | The logical name of the set (e.g. `"Alphabet"`, `"ABC"`) |
| `incrementalID` | `String` attribute | Auxiliary attribute for incremental tests; changing it acts as an "idle" structural edit that must not trigger a re-synchronisation |
| `elements` | containment reference `0..*` to `Element` | The (unordered) members of the set |

`Element` has a single `value : String` attribute.

### Target – `MyOrderedSet` (package `osets`)

| Feature | Type | Description |
|---------|------|-------------|
| `name` | `String` attribute | The logical name of the ordered set; must stay in sync with `MySet.name` |
| `incrementalID` | `String` attribute | Auxiliary attribute; same role as in `MySet` |
| `elements` | containment reference `0..*` to `Element` | The (ordered) members of the ordered set |

`Element` has a `value : String` attribute and two cross-references:

| Cross-reference | Description |
|----------------|-------------|
| `next` | Points to the successor `Element` in the doubly-linked list (absent on the last element) |
| `previous` | Points to the predecessor `Element` in the doubly-linked list (absent on the first element) |

The elements must form a valid, non-cyclic doubly-linked list from first to last.

---

## Named Model States (Resources)

All expected model states are stored as XMI files in the `resources/` directory.
Each state pair consists of a `*Set.xmi` (source) and a `*Oset.xmi` (target).
The `*CandidateOset.xmi` files are written transiently during `IncrementalForward` test execution.

| State name | Source (`*Set.xmi`) | Target (`*Oset.xmi`) | Description |
|------------|---------------------|----------------------|-------------|
| `RootElement` | empty `MySet` (no name, no elements) | empty `MyOrderedSet` (no name, no elements) | The very first state after `initiateSynchronisationDialogue()` |
| `EmptyAlphabet` | `name="Alphabet"`, no elements | `name="Alphabet"`, no elements | Empty set with the name "Alphabet" |
| `EmptyABC` | `name="ABC"`, no elements | `name="ABC"`, no elements | Empty set with the name "ABC" (renamed from "Alphabet") |
| `OnlyA` | `name="Alphabet"`, one element `A` | `name="Alphabet"`, one element `A` | Set with a single element |
| `FirstThreeLetters` | `name="Alphabet"`, elements `A`, `B`, `C` (unordered) | `name="Alphabet"`, elements linked `A→B→C` | Standard three-element state |
| `FirstThreeLettersChanged` | same as `FirstThreeLetters` but `incrementalID="changed"` | same as `FirstThreeLettersOset` but `incrementalID="changed"` | Used for stability/hippocraticness tests |
| `FirstThreeLettersChangedOset` | — | `A→B→C` with `incrementalID="changed"` | Target side of the changed-ID state |
| `Abcd` | `name="Alphabet"`, elements `A`, `B`, `C`, `D` | `name="Alphabet"`, elements linked `A→B→C→D` | Four-element state |
| `AbcdChanged` | same as `Abcd` but `incrementalID="changed"` | — | Used in incremental backward tests |
| `Ac` / `AcChanged` | `name="Alphabet"`, elements `A`, `C` | `name="Alphabet"`, elements linked `A→C` | Two-element state after deleting `B` and `D` |
| `C` / `CChanged` | `name="Alphabet"`, one element `C` | `name="Alphabet"`, one element `C` | Single-element state after further deletions |
| `Zxy` / `ZxyChanged` | `name="Alphabet"`, elements `Z`, `X`, `Y` | `name="Alphabet"`, elements linked `Z→X→Y` | Value-changed state (A→Z, B→X, C→Y) |
| `Cba` | — | `name="Alphabet"`, elements linked `C→B→A` | Target state after inverting `A→B→C` (used in hippocraticness test) |
| `acSet` / `abcdSet` | `{A,C}` / `{A,B,C,D}` (lower-case names, no specific `incrementalID`) | *(computed at runtime via candidate files)* | States used by `IncrementalForward` with order-agnostic assertions |

---

## Tool Implementations Under Test

Tool adapters live in `src/org/benchmarx/examples/set2oset/implementations/`.
The active set is defined in `Set2OsetTestCase.tools()`.

| Class | BX technology | Status |
|-------|--------------|--------|
| `BXtendSet2Oset` | **BXtend** (uses `BXtend-Set2OSet.jar`) | ✅ active |
| `MediniQVTSetToOSet` | **medini QVT** (uses `lib/mediniQVT/`, rule file `set2oset.qvt`) | ✅ active |
| `BXLangSet2Oset` | **BXLang** (uses generated `Sets2OrderedSetsTransformation`) | ✅ active |
| `BXAgentSet2OSet` | **BXAgent** (uses `Sets2OsetsTransformation` with post-processor for linked-list maintenance) | ✅ active |
| `IBeXTGGSetToOSet` | **IBeX TGG** | ⛔ commented out |

---

## Test Suite Structure

```
testsuite/
├── Set2OsetTestCase.java          # Base class; lifecycle helpers; tools() factory
├── Decisions.java                 # Empty enum (no non-deterministic choices)
├── BXToolParameterResolver.java   # JUnit 5 ParameterResolver for constructor injection
├── batch/
│   ├── fwd/BatchForward.java      # Batch forward propagation tests
│   └── bwd/BatchBackward.java     # Batch backward propagation tests
└── alignment_based/
    ├── fwd/IncrementalForward.java # Incremental forward tests (order-agnostic)
    └── bwd/IncrementalBackward.java# Incremental backward tests
```

---

## Test Classes and Test Cases

### 1. `BatchForward`

**Package:** `org.benchmarx.examples.set2oset.testsuite.batch.fwd`

Applies source-side edits on a fresh (empty) model and propagates them to the target in one batch
step (`performAndPropagateSourceEdit`).  After each propagation the complete source and target
models are compared against the named XMI resource pair.

---

#### `testInitialiseSynchronisation`

**What is tested:** That starting a synchronisation dialogue on an empty `MySet` (no name, no
elements) propagates to a matching, equally empty `MyOrderedSet`.

**Steps:**
1. Call `initialise()` — the tool creates empty source and target root objects and runs an
   initial batch propagation.

**Precondition:** none (no edit is performed before the assertion).

**Postcondition:** Source matches `RootElementSet.xmi` (empty `MySet`); target matches
`RootElementOset.xmi` (empty `MyOrderedSet`).

---

#### `testDatabaseNameChangeOfEmpty`

**What is tested:** That setting the name of an empty set (forward) produces a corresponding name
change in the ordered set.

**Steps:**
1. `performAndPropagateSourceEdit(helperSet::setSetName)` — sets `MySet.name = "Alphabet"` and
   propagates forward.

**Precondition:** Source = `EmptyAlphabetSet.xmi`; target = `EmptyAlphabetOset.xmi`
(both empty, name `"Alphabet"`).

2. `performAndPropagateSourceEdit(helperSet::renameAlphabetSetToABC)` — renames `MySet.name`
   from `"Alphabet"` to `"ABC"` and propagates forward.

**Postcondition:** Source = `EmptyABCSet.xmi`; target = `EmptyABCOset.xmi`
(both empty, name `"ABC"`).

---

#### `testCreateElement`

**What is tested:** That creating a single element `A` in the source and propagating forward
produces a matching singleton element in the target ordered set.

**Steps:**
1. `performAndPropagateSourceEdit(helperSet::setSetName)` — sets name to `"Alphabet"`.

**Precondition:** Source = `EmptyAlphabetSet.xmi`; target = `EmptyAlphabetOset.xmi`.

2. `performAndPropagateSourceEdit(helperSet::createA)` — adds element `A` to `MySet`.

**Postcondition:** Source = `OnlyASet.xmi` (`{A}`); target = `OnlyAOset.xmi` (one element `A`).

---

#### `testCreateMultipleElements`

**What is tested:** That creating three elements `A`, `B`, `C` in a single compound source edit
and propagating forward produces a valid ordered set with all three elements linked in a
doubly-linked list.

**Steps:**
1. `performAndPropagateSourceEdit(helperSet::setSetName)` — sets name to `"Alphabet"`.

**Precondition:** Source = `EmptyAlphabetSet.xmi`; target = `EmptyAlphabetOset.xmi`.

2. `performAndPropagateSourceEdit(createA + createB + createC)` — adds elements `A`, `B`, `C`
   in one compound edit.

**Postcondition:** Source = `FirstThreeLettersSet.xmi` (`{A,B,C}`); target =
`FirstThreeLettersOset.xmi` (`A→B→C`).

---

### 2. `BatchBackward`

**Package:** `org.benchmarx.examples.set2oset.testsuite.batch.bwd`

Mirrors `BatchForward`, but applies target-side edits (`performAndPropagateTargetEdit`) and
asserts that the source is updated accordingly.

---

#### `testInitialiseSynchronisation`

**What is tested:** Same as the forward variant — the initial empty-model synchronisation.

**Postcondition:** Source = `RootElementSet.xmi`; target = `RootElementOset.xmi`.

---

#### `testDatabaseNameChangeOfEmpty`

**What is tested:** That renaming the `MyOrderedSet` backward propagates the new name to
`MySet`.

**Steps:**
1. `performAndPropagateTargetEdit(helperOset::setSetName)` — sets `MyOrderedSet.name = "Alphabet"`.

**Precondition:** Source = `EmptyAlphabetSet.xmi`; target = `EmptyAlphabetOset.xmi`.

2. `performAndPropagateTargetEdit(helperOset::renameAlphabetSetToABC)` — renames ordered set
   to `"ABC"`.

**Postcondition:** Source = `EmptyABCSet.xmi`; target = `EmptyABCOset.xmi`.

---

#### `testCreateElement`

**What is tested:** That creating element `A` in the target and propagating backward adds `A`
to the source set.

**Steps:**
1. `performAndPropagateTargetEdit(helperOset::setSetName)`.

**Precondition:** `EmptyAlphabetSet` / `EmptyAlphabetOset`.

2. `performAndPropagateTargetEdit(helperOset::createA)`.

**Postcondition:** Source = `OnlyASet.xmi`; target = `OnlyAOset.xmi`.

---

#### `testCreateMultipleElements`

**What is tested:** That creating `A`, `B`, `C` in the target ordered set (appended in order)
and propagating backward adds all three elements to the source unordered set.

**Steps:**
1. `performAndPropagateTargetEdit(helperOset::setSetName)`.

**Precondition:** `EmptyAlphabetSet` / `EmptyAlphabetOset`.

2. `performAndPropagateTargetEdit(createA + createB + createC)` — appends elements in sequence
   to the linked list.

**Postcondition:** Source = `FirstThreeLettersSet.xmi`; target = `FirstThreeLettersOset.xmi`.

---

### 3. `IncrementalBackward`

**Package:** `org.benchmarx.examples.set2oset.testsuite.alignment_based.bwd`

Each test applies one or more target edits incrementally using `performAndPropagateTargetEdit`
on a pre-built model.  `performIdleSourceEdit` is used to set `incrementalID` on the source
so the tool has an alignment anchor without performing a structural change.

---

#### `testIncrementalInserts`

**What is tested:** That elements inserted at specific positions in the target ordered set are
correctly reflected as new elements in the source unordered set (regardless of insertion order).

**Setup:** Target is built with name `"Alphabet"` and one element `C`; source gets
`incrementalID = "changed"` as an idle edit.

**Precondition:** Source = `CChangedSet.xmi` (`{C}`, changed ID); target = `COset.xmi` (`C`).

**Step 1:** `insertABeforeC` — inserts `A` before `C` in the ordered set.  
**Postcondition:** Source = `AcChangedSet.xmi` (`{A,C}`); target = `AcOset.xmi` (`A→C`).

**Step 2:** `insertBBeforeC` — inserts `B` before `C` (making the list `A→B→C`).  
**Postcondition:** Source = `FirstThreeLettersChangedSet.xmi` (`{A,B,C}`); target =
`FirstThreeLettersOset.xmi` (`A→B→C`).

**Step 3:** `insertDAfterC` — inserts `D` after `C` (list becomes `A→B→C→D`).  
**Postcondition:** Source = `AbcdChangedSet.xmi` (`{A,B,C,D}`); target = `AbcdOset.xmi`
(`A→B→C→D`).

---

#### `testIncrementalDeletions`

**What is tested:** That elements deleted from the target ordered set are correctly removed from
the source unordered set.

**Setup:** Target built with elements `A`, `B`, `C`, `D`; source gets idle ID change.

**Precondition:** Source = `AbcdChangedSet.xmi`; target = `AbcdOset.xmi` (`A→B→C→D`).

**Step 1:** `deleteD` — removes element `D`.  
**Postcondition:** Source = `FirstThreeLettersChangedSet.xmi`; target = `FirstThreeLettersOset.xmi`.

**Step 2:** `deleteB` — removes element `B`.  
**Postcondition:** Source = `AcChangedSet.xmi`; target = `AcOset.xmi` (`A→C`).

**Step 3:** `deleteA` — removes element `A`.  
**Postcondition:** Source = `CChangedSet.xmi` (`{C}`); target = `COset.xmi`.

---

#### `testIncrementalValueChange`

**What is tested:** That renaming elements in the ordered set is reflected in the source set.

**Setup:** Target built with `A`, `B`, `C`; source gets idle ID change.

**Precondition:** Source = `FirstThreeLettersChangedSet.xmi`; target = `FirstThreeLettersOset.xmi`.

**Step:** `changeABCtoZXY` — renames `A→Z`, `B→X`, `C→Y` in the target.  
**Postcondition:** Source = `ZxyChangedSet.xmi` (`{Z,X,Y}`); target = `ZxyOset.xmi` (`Z→X→Y`).

---

#### `testStability`

**What is tested:** That applying an **idle** target edit (no structural change) does not alter
either model — i.e. the transformation is *stable*.

**Setup:** Target built with `A`, `B`, `C`; source gets idle ID change.

**Precondition:** Source = `FirstThreeLettersChangedSet.xmi`; target = `FirstThreeLettersOset.xmi`.

**Step:** `idleDelta` — no-op edit on the ordered set.  
**Postcondition:** Both models remain unchanged: `FirstThreeLettersChangedSet.xmi` /
`FirstThreeLettersOset.xmi`.

---

#### `testHippocraticness`

**What is tested:** That propagating a target edit which **only reorders** elements (inverts the
list from `A→B→C` to `C→B→A`) does **not** modify the source, because the source has no order
information. This verifies the *hippocraticness* property.

**Setup:** Target built with `A`, `B`, `C`; source gets idle ID change.

**Precondition:** Source = `FirstThreeLettersChangedSet.xmi`; target = `FirstThreeLettersOset.xmi`.

**Step:** `invert` — reverses the linked list order.  
**Postcondition:** Source = `FirstThreeLettersChangedSet.xmi` (unchanged — inversion has no
effect on an unordered set); target = `CbaOset.xmi` (`C→B→A`).

---

### 4. `IncrementalForward`

**Package:** `org.benchmarx.examples.set2oset.testsuite.alignment_based.fwd`

Forward incremental tests that account for the inherent **non-determinism** of forward
propagation: when a new element is inserted into an unordered source, the tool may place it at
*any* valid position in the ordered target.  Instead of asserting a single expected target, the
helper method `generatePossibleTargets()` computes the full set of valid target models from the
accumulated source deltas and a tracked last-known target, and the assertion passes as soon as
the actual target matches **any** candidate.

Candidates are written to `resources/*CandidateOset.xmi` at assertion time.

Target idle edits (`performIdleTargetEdit`) — e.g. inverting the ordered list — shift the
base target before the next round of source edits, thereby testing that the tool respects the
existing order after each change.

---

#### `testIncrementalInserts`

**What is tested:** That when elements are incrementally inserted into the source set, the tool
appends them to the target ordered set in some valid position, and that a subsequent idle target
inversion (`invert`) followed by a new insertion keeps existing elements in their (new) inverted
order.

**Step 1:** Compound source edit: name `"Alphabet"`, then add `A` and `C`.  
**Precondition:** Source = `acSet.xmi` (`{A,C}`); target matches any candidate ordered set
with elements `A` and `C` (the candidates cover all positions: `A→C` and `C→A`).

**Step 2:** Source edit: add `B`.  
**Postcondition:** Source = `FirstThreeLettersSet.xmi` (`{A,B,C}`); target matches any valid
ordering of `{A,B,C}` (6 candidates).

**Step 3 (idle target):** `invert` — reverses current target ordering; delta appended.

**Step 4:** Source edit: add `D`.  
**Postcondition:** Source = `abcdSet.xmi` (`{A,B,C,D}`); target matches any valid ordering of
`{A,B,C,D}` that extends the current (inverted) arrangement with `D` inserted at any position.

---

#### `testIncrementalDeletions`

**What is tested:** That deleting elements from the source removes the corresponding elements
from the target while preserving the remaining order, even after the target has been inverted.

**Step 1:** Compound source edit: name `"Alphabet"`, add `A`, `B`, `C`, `D`.  
**Precondition:** Source = `abcdSet.xmi`; target matches any valid ordering of `{A,B,C,D}`.

**Step 2:** Source edit: delete `D`.  
**Postcondition:** Source = `FirstThreeLettersSet.xmi`; target matches any valid ordering of
`{A,B,C}`.

**Step 3 (idle target):** `invert` — reverses current target ordering; delta appended.

**Step 4:** Source edit: delete `B`.  
**Postcondition:** Source = `acSet.xmi` (`{A,C}`); target matches any valid ordering of
`{A,C}` that is consistent with the (inverted) arrangement minus `B`.

---

#### `testIncrementalValueChange`

**What is tested:** That renaming all elements in the source (A→Z, B→X, C→Y and back again)
propagates the new values to the target while preserving order, and that a subsequent inversion
does not disturb that.

**Step 1:** Compound source edit: name `"Alphabet"`, add `A`, `B`, `C`.  
**Precondition:** Source = `FirstThreeLettersSet.xmi`; target matches any valid ordering of
`{A,B,C}`.

**Step 2:** Source edit: `changeABCtoZXY`.  
**Postcondition:** Source = `ZxySet.xmi` (`{Z,X,Y}`); target matches any valid ordering of
`{Z,X,Y}`.

**Step 3 (idle target):** `invert`.

**Step 4:** Source edit: `changeZXYtoABC` — rename `Z→A`, `X→B`, `Y→C`.  
**Postcondition:** Source = `FirstThreeLettersSet.xmi`; target matches any valid ordering of
`{A,B,C}` consistent with the (inverted) arrangement.

---

#### `testStability`

**What is tested:** That an **idle source** edit (no-op) does not change the target model when
the target has an incremental ID different from the default — verifies *stability*.

**Setup:** Source receives elements `A`, `B`, `C` via three individual propagations; an idle
target edit sets `incrementalID = "changed"` on the ordered set.

**Precondition:** Source = `FirstThreeLettersSet.xmi`; target = `FirstThreeLettersChangedOset.xmi`.

**Step:** `idleDelta` on source — a source edit that performs no modification.  
**Postcondition:** Both models unchanged: `FirstThreeLettersSet.xmi` /
`FirstThreeLettersChangedOset.xmi`.

---

#### `testHippocraticness`

**What is tested:** That a source edit which **only changes the source's `incrementalID`**
(no structural change to elements or name) does **not** change the target — verifies
*hippocraticness* in the forward direction.

**Setup:** Source receives elements `A`, `B`, `C`; an idle target edit changes target
`incrementalID`.

**Precondition:** Source = `FirstThreeLettersSet.xmi`; target = `FirstThreeLettersChangedOset.xmi`.

**Step:** `changeIncrementalID` on source.  
**Postcondition:** Source = `FirstThreeLettersChangedSet.xmi` (ID updated, structure same);
target = `FirstThreeLettersChangedOset.xmi` (unchanged).

---

## Helper Operations Reference

### `SetHelper` (source-side operations)

| Method | Delta type recorded | Effect on `MySet` |
|--------|--------------------|--------------------|
| `setSetName()` | `SetNameChange("Alphabet")` | Sets `name = "Alphabet"` |
| `renameAlphabetSetToABC()` | `SetNameChange("ABC")` | Renames from `"Alphabet"` to `"ABC"` |
| `changeIncrementalID()` | *(none — not a structural delta)* | Toggles `incrementalID` between `"changed"` and `"changed again"` |
| `createA()` | `ElementCreation("A")` | Adds element with `value = "A"` |
| `createB()` | `ElementCreation("B")` | Adds element with `value = "B"` |
| `createC()` | `ElementCreation("C")` | Adds element with `value = "C"` |
| `createD()` | `ElementCreation("D")` | Adds element with `value = "D"` |
| `deleteA()` | `ElementDeletion("A")` | Removes element `A` |
| `deleteB()` | `ElementDeletion("B")` | Removes element `B` |
| `deleteC()` | `ElementDeletion("C")` | Removes element `C` |
| `deleteD()` | `ElementDeletion("D")` | Removes element `D` |
| `changeABCtoZXY()` | `ElementChange("A","Z")` + `ElementChange("B","X")` + `ElementChange("C","Y")` | Renames A→Z, B→X, C→Y |
| `changeZXYtoABC()` | `ElementChange("Z","A")` + `ElementChange("X","B")` + `ElementChange("Y","C")` | Reverses the rename |
| `idleDelta()` | *(no delta, no change)* | No-op |

### `OsetHelper` (target-side operations)

| Method | Delta type recorded | Effect on `MyOrderedSet` |
|--------|--------------------|-----------------------------|
| `setSetName()` | *(none)* | Sets `name = "Alphabet"` |
| `renameAlphabetSetToABC()` | *(none)* | Renames from `"Alphabet"` to `"ABC"` |
| `changeIncrementalID()` | *(none)* | Toggles `incrementalID` |
| `createA()` | *(none)* | Appends element `A` at end of linked list |
| `createB()` | *(none)* | Appends element `B` at end of linked list |
| `createC()` | *(none)* | Appends element `C` at end of linked list |
| `createD()` | *(none)* | Appends element `D` at end of linked list |
| `deleteA()` | *(none)* | Removes element `A` and re-links list |
| `deleteB()` | *(none)* | Removes element `B` and re-links list |
| `deleteC()` | *(none)* | Removes element `C` and re-links list |
| `deleteD()` | *(none)* | Removes element `D` and re-links list |
| `insertABeforeC()` | *(none)* | Inserts new element `A` immediately before `C` |
| `insertBBeforeC()` | *(none)* | Inserts new element `B` immediately before `C` |
| `insertDAfterC()` | *(none)* | Inserts new element `D` immediately after `C` |
| `changeABCtoZXY()` | *(none)* | Renames A→Z, B→X, C→Y in-place |
| `invert()` | `OsetElementsInversion` | Reverses the doubly-linked list order |
| `idleDelta()` | *(no delta, no change)* | No-op |

---

## Running the Tests

The project is an Eclipse PDE plug-in project.  There is no standalone Maven/Gradle build.

1. Import the project set: `File > Import > Team > Team Project Set` → select
   `examples/settooset/projectSet.psf`.
2. Select project `BenchmarxSetToOSet` → **Run As > JUnit Test**.
3. To run a single class, right-click it in the Package Explorer → **Run As > JUnit Test**.

To add or remove a tool from the benchmark, edit the `tools()` method in
`Set2OsetTestCase.java`.
