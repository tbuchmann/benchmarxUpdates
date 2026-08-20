# BenchmarX Bag1-to-Bag2 Test Suite

This project contains the **BenchmarX** test suite for the bidirectional transformation between two Bag metamodels: **Bag1** (`bags1.MyBag`) and **Bag2** (`bags2.MyBag`). The transformation is tested using two BX tool implementations: **BXLang** and **BXAgent**.

---

## Metamodels

### Bag1 (`bags1`, package URI: `http://de.ubt.ai1.bw.qvt.examples.bags1.ecore`)

Bag1 is an **uncompressed** bag: each element occurrence is represented as a separate `Element` object.

| Class | Attributes / References | Description |
|---|---|---|
| `MyBag` | `elements : Element[*]` | Root container; holds a list of individual element objects |
| `Element` | `value : EString` | The string value of this occurrence (e.g., `"Beer"`, `"BeerGlass"`) |
| | `incrementalID : EString` (default: `"default"`) | Auxiliary ID used to track element identity across incremental edits |
| | `bag : MyBag` | Back-reference to the owning bag |

### Bag2 (`bags2`, package URI: `http://de.ubt.ai1.bw.qvt.examples.bags2.ecore`)

Bag2 is a **compressed** bag: elements with the same value are grouped into a single `Element` object with an explicit multiplicity.

| Class | Attributes / References | Description |
|---|---|---|
| `MyBag` | `elements : Element[*]` | Root container; holds one representative object per distinct value |
| `Element` | `value : EString` | The string value shared by all occurrences in this group |
| | `multiplicity : EInt` | Number of occurrences of this value in the bag |
| | `incrementalID : EString` | Auxiliary ID used to track element identity across incremental edits |
| | `bag : MyBag` | Back-reference to the owning bag |

### Transformation Semantics

The transformation maps an uncompressed Bag1 to a compressed Bag2:
- All `Element` objects in Bag1 with the same `value` are merged into a single `Element` in Bag2 whose `multiplicity` equals the number of occurrences.
- Conversely, a single Bag2 `Element` with `multiplicity = n` expands to `n` individual Bag1 `Element` objects with the same `value`.

---

## BX Tool Implementations

All tests are executed as JUnit 5 `@ParameterizedTest` and run against the following tool implementations:

| Tool | Class | Description |
|---|---|---|
| **BXLang** | `BXLangBag12Bag2` | Uses the BXLang DSL (`Bags1ToBags2Transformation`) for both forward and backward propagation |
| **BXAgent** | `BXAgentBags2Bags` | Uses the BXAgent framework (`Bags12Bags2Transformation`) with correspondence-model-based incremental propagation and `CASCADE` deletion policy |

The following implementations exist in the source tree but are **not currently active**:
- `BXtendBag12Bag2` (BXtend DSL)
- `MediniQVTBag12Bag2` (MediniQVT)

---

## Test Suite Structure

The tests are organised along two orthogonal dimensions:

| Dimension | Values |
|---|---|
| **Propagation direction** | **Forward** (Bag1 → Bag2) · **Backward** (Bag2 → Bag1) |
| **Synchronisation mode** | **Batch** (empty starting state) · **Incremental / alignment-based** (non-empty starting state, existing correspondences are preserved) |

This yields four test classes:

| Class | Package | Direction | Mode |
|---|---|---|---|
| `BatchForward` | `…testsuite.batch.fwd` | Forward | Batch |
| `BatchBackward` | `…testsuite.batch.bwd` | Backward | Batch |
| `IncrementalForward` | `…testsuite.alignment_based.fwd` | Forward | Incremental |
| `IncrementalBackward` | `…testsuite.alignment_based.bwd` | Backward | Incremental |

---

## Test Cases

### 1 · `BatchForward` — Batch Forward Tests

Start from **empty models**; edits are made in Bag1 and propagated to Bag2.

---

#### 1.1 `testInitialiseSynchronisation`

**What is tested:**
After the synchronisation dialogue is initiated (both models are empty), the transformation must produce well-formed root elements in both models without any explicit edit being performed.

**Precondition:** None (empty models).

**Steps:**
1. Initialise the BX tool (empty Bag1 and Bag2 are created).
2. No edit is applied.
3. Assert postcondition.

**Expected result:**
- Bag1 matches reference state `RootElementBags1` — a `MyBag` root with no elements.
- Bag2 matches reference state `RootElementBags2` — a `MyBag` root with no elements.

---

#### 1.2 `testCreateElement`

**What is tested:**
Creating a single element with value `"Beer"` in an empty Bag1 must be correctly propagated forward to Bag2, producing one grouped element with multiplicity 1.

**Precondition:** None (empty models).

**Steps:**
1. Initialise the BX tool.
2. Source edit: create one Beer element in Bag1 (`createOneBeer`).
3. Propagate forward (`performAndPropagateSourceEdit`).
4. Assert postcondition.

**Expected result:**
- Bag1 matches `OneBeerBags1` — one `Element` with `value = "Beer"`.
- Bag2 matches `OneBeerBags2` — one `Element` with `value = "Beer"` and `multiplicity = 1`.

---

#### 1.3 `testCreateMultipleElements`

**What is tested:**
Creating multiple elements of two distinct types (5 Beers and 1 Beer Glass) in an empty Bag1 must produce the correct compressed representation in Bag2 — one entry per distinct value with the corresponding multiplicity.

**Precondition:** None (empty models).

**Steps:**
1. Initialise the BX tool.
2. Source edit: create five Beer elements (`createFiveBeers`) and one Beer Glass element (`createBeerGlass`) in Bag1.
3. Propagate forward.
4. Assert postcondition.

**Expected result:**
- Bag1 matches `FiveBeerWithGlassBags1` — five `Element` objects with `value = "Beer"` and one with `value = "BeerGlass"`.
- Bag2 matches `FiveBeerWithGlassBags2` — one entry `(Beer, 5)` and one entry `(BeerGlass, 1)`.

---

### 2 · `BatchBackward` — Batch Backward Tests

Start from **empty models**; edits are made in Bag2 and propagated back to Bag1.

---

#### 2.1 `testCreateElement`

**What is tested:**
Creating a single element with value `"Beer"` in an empty Bag2 must be correctly back-propagated to Bag1, expanding the single grouped entry into individual element objects.

**Precondition:** None (empty models).

**Steps:**
1. Initialise the BX tool.
2. Target edit: create one Beer element in Bag2 (`createOneBeer`).
3. Propagate backward (`performAndPropagateTargetEdit`).
4. Assert postcondition.

**Expected result:**
- Bag1 matches `OneBeerBags1` — one `Element` with `value = "Beer"`.
- Bag2 matches `OneBeerBags2` — one `Element` with `value = "Beer"` and `multiplicity = 1`.

---

#### 2.2 `testCreateMultipleElements`

**What is tested:**
Creating multiple elements of two distinct types (5 Beers and 1 Beer Glass) in an empty Bag2 must back-propagate correctly to Bag1, expanding each group entry into the corresponding number of individual element objects.

**Precondition:** None (empty models).

**Steps:**
1. Initialise the BX tool.
2. Target edit: create five Beer elements (`createFiveBeer`) and one Beer Glass element (`createBeerGlass`) in Bag2.
3. Propagate backward.
4. Assert postcondition.

**Expected result:**
- Bag1 matches `FiveBeerWithGlassBags1` — five Beer elements and one Beer Glass element.
- Bag2 matches `FiveBeerWithGlassBags2` — one entry `(Beer, 5)` and one entry `(BeerGlass, 1)`.

---

### 3 · `IncrementalForward` — Incremental (Alignment-Based) Forward Tests

Start from a **non-empty consistent state**; further edits are made in Bag1 and propagated to Bag2. Correspondences between existing elements are taken into account (corr-based / alignment-based).

---

#### 3.1 `testIncrementalInserts`

**What is tested:**
Adding more elements to an already-populated Bag1 must correctly augment the existing Bag2 entries — increasing the multiplicity of the existing Beer entry and creating a new entry for Beer Glass — without disturbing pre-existing elements.

**Precondition:**
- Bag1: one Beer element (`OneBeerBags1`).
- Bag2: one entry `(Beer, 1)` with incremental ID set (`OneBeerIncrIDBags2`).

**Steps:**
1. Initialise the BX tool.
2. Source edit → propagate: create one Beer in Bag1; set incremental ID in Bag2 (`changeIncrementalID`).
3. Assert precondition.
4. Source edit: add five more Beers (`createFiveBeers`) and two Beer Glasses (`createBeerGlass` × 2) to Bag1.
5. Propagate forward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `SixBeerWithTwoGlassesBags1` — six Beer elements and two Beer Glass elements.
- Bag2 matches `SixBeerWithTwoGlassesBags2` — one entry `(Beer, 6)` and one entry `(BeerGlass, 2)`.

---

#### 3.2 `testIncrementalDeletions`

**What is tested:**
Deleting elements from Bag1 must correctly reduce the multiplicity of the corresponding Bag2 entry, and remove the entry entirely when the multiplicity reaches zero.

**Precondition:**
- Bag1: five Beer elements and one Beer Glass element (`FiveBeerWithGlassBags1`).
- Bag2: entry `(Beer, 5)` and entry `(BeerGlass, 1)` with incremental ID (`FiveBeerWithGlassIncrIDBags2`).

**Steps:**
1. Initialise the BX tool.
2. Source edit → propagate: create five Beers and one Beer Glass in Bag1; set incremental ID in Bag2.
3. Assert precondition.
4. Source edit: delete two Beer elements (`deleteBeer` × 2) and one Beer Glass element (`deleteBeerGlass`) from Bag1.
5. Propagate forward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `ThreeBeerBags1` — three Beer elements, no Beer Glass.
- Bag2 matches `ThreeBeerBags2` — one entry `(Beer, 3)`, no Beer Glass entry.

---

#### 3.3 `testIncrementalValueChangeOfOne`

**What is tested:**
Changing the `value` attribute of exactly one Beer element in Bag1 to `"EmptyBottle"` must produce a separate entry for Empty Bottle in Bag2 (with multiplicity 1) and reduce the Beer entry's multiplicity by one, while the Beer Glass entry remains unchanged.

**Precondition:**
- Bag1: five Beer elements and one Beer Glass element (`FiveBeerWithGlassBags1`).
- Bag2: entry `(Beer, 5)` and entry `(BeerGlass, 1)` with incremental ID (`FiveBeerWithGlassIncrIDBags2`).

**Steps:**
1. Initialise the BX tool.
2. Source edit → propagate: create five Beers and one Beer Glass; set incremental ID in Bag2.
3. Assert precondition.
4. Source edit: change one Beer element's value to `"EmptyBottle"` (`changeOneBeerToEmptyBottle`); also update incremental ID in Bag1 (`changeIncrementalID`).
5. Propagate forward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `FourBeerOneEmptyBottleWithGlassBags1` — four Beer, one Empty Bottle, one Beer Glass.
- Bag2 matches `FourBeerOneEmptyBottleWithGlassIncrIDBags2` — entries `(Beer, 4)`, `(EmptyBottle, 1)`, `(BeerGlass, 1)` with updated incremental ID.

---

#### 3.4 `testIncrementalValueChangeOfAll`

**What is tested:**
Changing the `value` attribute of **all** Beer elements in Bag1 to `"EmptyBottle"` at once must consolidate all occurrences into a single Bag2 entry `(EmptyBottle, 5)` and remove the Beer entry, while the Beer Glass entry is unaffected.

> **Note (known behaviour):** Because all values change simultaneously, the BXLang runtime may not detect the value-change as a pure attribute update but instead invalidates and recreates the corresponding Bag2 entry. See `TestFailures.md` for details.

**Precondition:**
- Bag1: five Beer elements and one Beer Glass element (`FiveBeerWithGlassBags1`).
- Bag2: entry `(Beer, 5)` and entry `(BeerGlass, 1)` with incremental ID (`FiveBeerWithGlassIncrIDBags2`).

**Steps:**
1. Initialise the BX tool.
2. Source edit → propagate: create five Beers and one Beer Glass; set incremental ID in Bag2.
3. Assert precondition.
4. Source edit: change all Beer elements to `"EmptyBottle"` (`changeAllBeerToEmptyBottles`).
5. Propagate forward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `FiveEmptyBottlesWithGlassBags1` — five Empty Bottle elements and one Beer Glass element.
- Bag2 matches `FiveEmptyBottlesWithGlassBags2` — entries `(EmptyBottle, 5)` and `(BeerGlass, 1)`.

---

#### 3.5 `testStability`

**What is tested:**
Re-running the forward propagation after an **idle source edit** (no actual change) must leave Bag2 completely unchanged. This verifies the **hippocraticness** (stability / no-op) property of the transformation.

**Precondition:**
- Bag1: six Beer elements and two Beer Glass elements (`SixBeerWithTwoGlassesBags1`).
- Bag2: entries `(Beer, 6)` and `(BeerGlass, 2)` with incremental ID (`SixBeerWithTwoGlassesBags2`).

**Steps:**
1. Initialise the BX tool.
2. Source edit → propagate: create five Beers + one Beer + two Beer Glasses; set incremental ID in Bag2.
3. Assert precondition.
4. Source edit: apply idle delta to Bag1 (`idleDelta` — no actual change).
5. Propagate forward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `SixBeerWithTwoGlassesBags1` — unchanged.
- Bag2 matches `SixBeerWithTwoGlassesBags2` — unchanged.

---

### 4 · `IncrementalBackward` — Incremental (Alignment-Based) Backward Tests

Start from a **non-empty consistent state**; further edits are made in Bag2 and propagated back to Bag1. Correspondences between existing elements are taken into account.

---

#### 4.1 `testIncrementalInserts`

**What is tested:**
Adding elements to an already-populated Bag2 must correctly expand the new entries into individual element objects in Bag1, without altering the pre-existing Bag1 elements.

**Precondition:**
- Bag2: one entry `(EmptyBottle, 1)` (`OneEmptyBottleBags2`).
- Bag1: one Empty Bottle element with incremental ID set (`OneEmptyBottleBags1`).

**Steps:**
1. Initialise the BX tool.
2. Target edit → propagate: create one Empty Bottle in Bag2; set incremental ID in Bag1 (`changeIncrementalID`).
3. Assert precondition.
4. Target edit: add one Beer Glass (`createBeerGlass`) and four Beer entries (`createFourBeer`) to Bag2.
5. Propagate backward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `FourBeerOneEmptyBottleWithGlassBags1` — four Beer elements, one Empty Bottle element, one Beer Glass element.
- Bag2 matches `FourBeerOneEmptyBottleWithGlassBags2` — entries `(Beer, 4)`, `(EmptyBottle, 1)`, `(BeerGlass, 1)`.

---

#### 4.2 `testIncrementalDeletions`

**What is tested:**
Deleting entries from Bag2 (Beer Glass and all Beers) must correctly remove the corresponding individual element objects from Bag1, leaving the Empty Bottle element untouched.

**Precondition:**
- Bag2: entries `(Beer, 4)`, `(BeerGlass, 1)`, `(EmptyBottle, 1)` (`FourBeerOneEmptyBottleWithGlassBags2`).
- Bag1: four Beer elements, one Beer Glass element, one Empty Bottle element with incremental ID set (`FourBeerOneEmptyBottleWithGlassBags1`).

**Steps:**
1. Initialise the BX tool.
2. Target edit → propagate: create one Beer Glass, four Beers, and one Empty Bottle in Bag2; set incremental ID in Bag1.
3. Assert precondition.
4. Target edit: delete the Beer Glass entry (`deleteBeerGlass`) and all Beer entries (`deleteAllBeers`) from Bag2.
5. Propagate backward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `OneEmptyBottleBags1` — only one Empty Bottle element remains.
- Bag2 matches `OneEmptyBottleBags2` — only entry `(EmptyBottle, 1)` remains.

---

#### 4.3 `testIncrementalValueChangeOfAll`

**What is tested:**
Performing a combination of value changes and multiplicity changes in Bag2 simultaneously must back-propagate all changes correctly to Bag1. The four combined changes are:
1. `EmptyBottle` → `BrokenBottle` (value change).
2. Reduce the multiplicity of `Beer` from 4 to 2 (multiplicity change).
3. `Beer` → `EmptyBottle` (value change — the remaining 2 Beer entries change value).
4. Increase the multiplicity of `BeerGlass` from 1 to 2 (multiplicity change).

**Precondition:**
- Bag2: entries `(Beer, 4)`, `(BeerGlass, 1)`, `(EmptyBottle, 1)` (`FourBeerOneEmptyBottleWithGlassBags2`).
- Bag1: four Beer elements, one Beer Glass element, one Empty Bottle element with incremental ID set (`FourBeerOneEmptyBottleWithGlassBags1`).

**Steps:**
1. Initialise the BX tool.
2. Target edit → propagate: create one Beer Glass, four Beers, and one Empty Bottle in Bag2; set incremental ID in Bag1.
3. Assert precondition.
4. Target edit (combined):
   - `changeEmptyBottleToBrokenBottle` — rename the Empty Bottle entry to Broken Bottle.
   - `changeMultiplicityOfBeer` — reduce Beer multiplicity to 2.
   - `changeBeerToEmptyBottle` — rename the Beer entry to Empty Bottle.
   - `changeMultiplicityOfBeerGlass` — increase Beer Glass multiplicity to 2.
5. Propagate backward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags1` — one Broken Bottle, two Empty Bottle elements, two Beer Glass elements.
- Bag2 matches `OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags2` — entries `(BrokenBottle, 1)`, `(EmptyBottle, 2)`, `(BeerGlass, 2)`.

---

#### 4.4 `testStability`

**What is tested:**
Re-running the backward propagation after an **idle target edit** (no actual change) must leave Bag1 completely unchanged. This verifies the **hippocraticness** (stability / no-op) property of the backward transformation.

**Precondition:**
- Bag2: entries `(Beer, 4)`, `(BeerGlass, 1)`, `(EmptyBottle, 1)` (`FourBeerOneEmptyBottleWithGlassBags2`).
- Bag1: four Beer elements, one Beer Glass element, one Empty Bottle element with incremental ID set (`FourBeerOneEmptyBottleWithGlassBags1`).

**Steps:**
1. Initialise the BX tool.
2. Target edit → propagate: create one Beer Glass, four Beers, and one Empty Bottle in Bag2; set incremental ID in Bag1.
3. Assert precondition.
4. Target edit: apply idle delta to Bag2 (`idleDelta` — no actual change).
5. Propagate backward.
6. Assert postcondition.

**Expected result:**
- Bag1 matches `FourBeerOneEmptyBottleWithGlassBags1` — unchanged.
- Bag2 matches `FourBeerOneEmptyBottleWithGlassBags2` — unchanged.

---

## Summary of All Test Cases

| # | Class | Method | Direction | Mode | Core aspect tested |
|---|---|---|---|---|---|
| 1 | `BatchForward` | `testInitialiseSynchronisation` | fwd | Batch | Correct root elements after init |
| 2 | `BatchForward` | `testCreateElement` | fwd | Batch | Forward creation of a single element |
| 3 | `BatchForward` | `testCreateMultipleElements` | fwd | Batch | Forward creation of multiple elements of two types |
| 4 | `BatchBackward` | `testCreateElement` | bwd | Batch | Backward creation of a single element |
| 5 | `BatchBackward` | `testCreateMultipleElements` | bwd | Batch | Backward creation of multiple elements of two types |
| 6 | `IncrementalForward` | `testIncrementalInserts` | fwd | Incremental | Adding elements to an existing Bag1 |
| 7 | `IncrementalForward` | `testIncrementalDeletions` | fwd | Incremental | Deleting elements from an existing Bag1 |
| 8 | `IncrementalForward` | `testIncrementalValueChangeOfOne` | fwd | Incremental | Changing the value of a single Bag1 element |
| 9 | `IncrementalForward` | `testIncrementalValueChangeOfAll` | fwd | Incremental | Changing the value of all occurrences in Bag1 |
| 10 | `IncrementalForward` | `testStability` | fwd | Incremental | Hippocraticness: idle forward delta leaves Bag2 unchanged |
| 11 | `IncrementalBackward` | `testIncrementalInserts` | bwd | Incremental | Adding entries to an existing Bag2 |
| 12 | `IncrementalBackward` | `testIncrementalDeletions` | bwd | Incremental | Deleting entries from an existing Bag2 |
| 13 | `IncrementalBackward` | `testIncrementalValueChangeOfAll` | bwd | Incremental | Combined value and multiplicity changes in Bag2 |
| 14 | `IncrementalBackward` | `testStability` | bwd | Incremental | Hippocraticness: idle backward delta leaves Bag1 unchanged |

---

## Reference Model States

The postconditions and preconditions reference named XMI files stored in the `resources/` directory:

| Identifier | Bag | Contents |
|---|---|---|
| `RootElementBags1` | Bag1 | Empty `MyBag` root, no elements |
| `RootElementBags2` | Bag2 | Empty `MyBag` root, no elements |
| `OneBeerBags1` | Bag1 | One `Element(value="Beer")` |
| `OneBeerBags2` | Bag2 | One `Element(value="Beer", multiplicity=1)` |
| `OneBeerIncrIDBags2` | Bag2 | One `Element(value="Beer", multiplicity=1)` with incremental ID set |
| `FiveBeerWithGlassBags1` | Bag1 | Five `Element(value="Beer")` + one `Element(value="BeerGlass")` |
| `FiveBeerWithGlassBags2` | Bag2 | `(Beer,5)` + `(BeerGlass,1)` |
| `FiveBeerWithGlassIncrIDBags2` | Bag2 | `(Beer,5)` + `(BeerGlass,1)` with incremental ID set |
| `SixBeerWithTwoGlassesBags1` | Bag1 | Six `Element(value="Beer")` + two `Element(value="BeerGlass")` |
| `SixBeerWithTwoGlassesBags2` | Bag2 | `(Beer,6)` + `(BeerGlass,2)` |
| `ThreeBeerBags1` | Bag1 | Three `Element(value="Beer")` |
| `ThreeBeerBags2` | Bag2 | `(Beer,3)` |
| `FourBeerOneEmptyBottleWithGlassBags1` | Bag1 | Four Beer + one Empty Bottle + one Beer Glass elements |
| `FourBeerOneEmptyBottleWithGlassBags2` | Bag2 | `(Beer,4)` + `(EmptyBottle,1)` + `(BeerGlass,1)` |
| `FourBeerOneEmptyBottleWithGlassIncrIDBags2` | Bag2 | `(Beer,4)` + `(EmptyBottle,1)` + `(BeerGlass,1)` with incremental ID set |
| `FiveEmptyBottlesWithGlassBags1` | Bag1 | Five Empty Bottle elements + one Beer Glass element |
| `FiveEmptyBottlesWithGlassBags2` | Bag2 | `(EmptyBottle,5)` + `(BeerGlass,1)` |
| `OneEmptyBottleBags1` | Bag1 | One `Element(value="EmptyBottle")` |
| `OneEmptyBottleBags2` | Bag2 | `(EmptyBottle,1)` |
| `OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags1` | Bag1 | One Broken Bottle + two Empty Bottle + two Beer Glass elements |
| `OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags2` | Bag2 | `(BrokenBottle,1)` + `(EmptyBottle,2)` + `(BeerGlass,2)` |

---

## Known Test Issues

See [`TestFailures.md`](TestFailures.md) for a list of known failures and their causes.

In particular, `IncrementalForward.testIncrementalValueChangeOfAll` is known to behave non-optimally in BXLang: when all elements of the same value are simultaneously renamed, the runtime does not detect this as a pure attribute-value update. Instead, it determines that the source and target values no longer correspond, invalidates the existing correspondence, and recreates the Bag2 entry from scratch. The test still passes (the final model state is correct), but the operation is less efficient than a pure value-change propagation would be.
