# BenchmarxPdb1ToPdb2

## Overview

This project contains the **Benchmarx** test suite for the **PDB1 ↔ PDB2** bidirectional transformation (BX). The transformation synchronises two structurally similar, yet schema-incompatible, person-database models:

- **PDB1** (`pdb1.Database`) – a "split-name" database where each person carries separate `firstName` and `lastName` attributes.
- **PDB2** (`pdb2.Database`) – a "full-name" database where each person stores the complete name in a single `name` attribute.

The key challenge of this transformation is that the name-splitting direction (PDB2 → PDB1) is inherently **non-deterministic**: given a full name such as `"Konrad Hermann Joseph Adenauer"`, it is not clear where the first name ends and the last name begins. This ambiguity is resolved by a **runtime decision** (see [Decisions](#decisions)).

---

## Metamodels

### PDB1 (`PersonsDB1.ecore`)

| Class | Attributes / References |
|---|---|
| `Database` | `name : EString`, `persons : Person[*]` (containment) |
| `Person` | `firstName : EString`, `lastName : EString`, `birthday : EString`, `placeOfBirth : EString`, `id : EString`, `incrementalID : EString`, `database : Database` (opposite) |

### PDB2 (`PersonsDB2.ecore`)

| Class | Attributes / References |
|---|---|
| `Database` | `name : EString`, `persons : Person[*]` (containment) |
| `Person` | `name : EString`, `birthday : EString`, `placeOfBirth : EString`, `id : EString`, `incrementalID : EString`, `database : Database` (opposite) |

**Key structural difference:** `Person.firstName` + `Person.lastName` in PDB1 correspond to the single `Person.name` in PDB2. The forward direction (PDB1 → PDB2) concatenates both name parts: `name = firstName + " " + lastName`. The backward direction (PDB2 → PDB1) must split the `name` string at a space character, with the split point controlled by the `PREFER_USING_FIRST_SPACE_TO_LAST` decision flag.

---

## Decisions

The enum `org.benchmarx.examples.pdb12pdb2.testsuite.Decisions` defines one runtime decision:

| Decision | Meaning |
|---|---|
| `PREFER_USING_FIRST_SPACE_TO_LAST` | Controls how a PDB2 full name is split into `firstName` / `lastName` when propagating backwards (PDB2 → PDB1). **`true`** → split at the **first** space (one first name, all remaining tokens become last name). **`false`** → split at the **last** space (all leading tokens become first name, one last name token). |

**Example – `"Konrad Hermann Joseph Adenauer"`:**

| Decision | `firstName` | `lastName` |
|---|---|---|
| `false` (last space, default) | `Konrad Hermann Joseph` | `Adenauer` |
| `true` (first space) | `Konrad` | `Hermann Joseph Adenauer` |

---

## Tools Under Test

The test suite is parameterised over all registered BX tools. The currently active tools (see `Pdb12Pdb2TestCase.tools()`) are:

| Tool class | Technology |
|---|---|
| `BXAgentPdb12Pdb2` | BX-Agent |
| `BXLangPdb12Pdb2` | BXLang |

Commented-out entries (currently disabled): `BXtendPdb12Pdb2`, `JavaPdb12Pdb2`.

---

## Test Infrastructure

- **`Pdb12Pdb2TestCase`** – Abstract base class. Initialises the EMF packages, the `BenchmarxUtil` helper, the PDB1/PDB2 model helpers (`Pdb1Helper`, `Pdb2Helper`), and the BX tool. Also provides the `srcEdit(...)` / `trgEdit(...)` factory methods for composing edit sequences.
- **`BXToolParameterResolver`** – JUnit 5 extension that injects the parameterised `BXTool` instances into each test constructor.
- **`Pdb1Helper` / `Pdb2Helper`** – Domain helpers that create, modify, and delete `Person` and `Database` objects and record the corresponding edit steps.
- **Model states (`.xmi` files in `resources/`)** – XMI snapshots used as pre- and postconditions in assertions.

---

## Test Suite Structure

```
testsuite/
├── batch/
│   ├── fwd/  BatchForward          (4 tests)
│   └── bwd/  BatchBackwardFixed    (2 tests)
│              BatchBackwardNotFirst (2 tests)
│              BatchBackwardFirst    (2 tests)
└── alignment_based/
    ├── fwd/  IncrementalForward    (5 tests)
    └── bwd/  IncrementalBackward   (7 tests)
```

Total: **22 test methods**, each executed once per registered BX tool.

---

## Test Classes and Test Cases

### 1. `BatchForward` — Batch Forward (PDB1 → PDB2)

Batch tests start from an agreed-upon initial state and propagate source-side edits to the target. Feature tag: **fwd, fixed** (unless noted).

---

#### `testInitialiseSynchronisation`

**What is tested:** Verification of the agreed-upon synchronisation starting state.

**Steps:**
1. Initialise the synchronisation dialogue (no edits performed).

**Precondition:** none (empty models).

**Expected postcondition:** Both models contain only their respective root `Database` elements with no persons and no database name set.
- PDB1: `RootElementPdb1` — empty `Database`, no name, no persons.
- PDB2: `RootElementPdb2` — empty `Database`, no name, no persons.

---

#### `testDatabaseNameChangeOfEmpty`

**What is tested:** Renaming of an empty PDB1 database and propagation of the new name to PDB2.

**Steps:**
1. Perform a source edit: set the database name to `"Bundeskanzler"`.
2. *(This edit establishes the precondition — note the Javadoc remark that this step is not strictly a batch test.)*
3. Perform a second source edit: rename the database from `"Bundeskanzler"` to `"Bundespräsidenten"`.

**Precondition:** Both models hold an empty database named `"Bundeskanzler"`.
- PDB1: `EmptyBundeskanzlerPdb1`
- PDB2: `EmptyBundeskanzlerPdb2`

**Expected postcondition:** Both models hold an empty database renamed to `"Bundespräsidenten"`.
- PDB1: `EmptyBundespräsidentenPdb1`
- PDB2: `EmptyBundespräsidentenPdb2`

---

#### `testCreatePerson`

**What is tested:** Creation of a single person (Konrad Adenauer) from scratch in PDB1 and forward propagation to PDB2.

**Steps:**
1. Perform a source edit: create the person `Konrad Adenauer` in the empty PDB1 database
   (`firstName="Konrad Hermann Joseph"`, `lastName="Adenauer"`, `birthday="05.01.1876"`, `placeOfBirth="Koeln"`, `id="KA"`).

**Precondition:** none (empty models).

**Expected postcondition:**
- PDB1 (`AdenauerPdb1`): Database contains one person with `firstName="Konrad Hermann Joseph"`, `lastName="Adenauer"`, `birthday="05.01.1876"`, `placeOfBirth="Koeln"`, `id="KA"`.
- PDB2 (`AdenauerPdb2`): Database contains one person with `name="Konrad Hermann Joseph Adenauer"`, `birthday="05.01.1876"`, `placeOfBirth="Koeln"`, `id="KA"`.

This directly verifies the forward name-concatenation rule.

---

#### `testCreateMultiplePersons`

**What is tested:** Batch creation of three persons (the first three German Federal Chancellors) in PDB1 and forward propagation to PDB2.

**Steps:**
1. Perform a source edit: set the database name to `"Bundeskanzler"` *(establishes precondition; not itself a batch step)*.
2. Perform a source edit that atomically creates three persons:
   - Konrad Adenauer (`firstName="Konrad Hermann Joseph"`, `lastName="Adenauer"`, `birthday="05.01.1876"`, `placeOfBirth="Koeln"`, `id="KA"`)
   - Ludwig Erhard (`firstName="Ludwig Wilhelm"`, `lastName="Erhard"`, `birthday="04.02.1897"`, `placeOfBirth="Fuerth"`, `id="LE"`)
   - Kurt Kiesinger (`firstName="Kurt Georg"`, `lastName="Kiesinger"`, `birthday="06.04.1904"`, `placeOfBirth="Ebingen"`, `id="KK"`)

**Precondition:**
- PDB1 (`EmptyBundeskanzlerPdb1`): empty database, `name="Bundeskanzler"`.
- PDB2 (`EmptyBundeskanzlerPdb2`): empty database, `name="Bundeskanzler"`.

**Expected postcondition:**
- PDB1 (`PDB1FirstThreeChancellors`): database `name="Bundeskanzler"` with all three persons, `firstName`/`lastName` split.
- PDB2 (`Pre_IncrBwdPDB2FirstThreeChancellors`): database `name="Bundeskanzler"` with all three persons and full concatenated names (`"Konrad Hermann Joseph Adenauer"`, `"Ludwig Wilhelm Erhard"`, `"Kurt Georg Kiesinger"`).

---

### 2. `BatchBackwardFixed` — Batch Backward with Fixed Decision (PDB2 → PDB1)

Batch tests propagating target-side edits to the source; the name-splitting decision is not set explicitly (relies on the tool's default). Feature tag: **bwd, fixed**.

---

#### `testInitialiseSynchronisation`

**What is tested:** Verification of the agreed-upon synchronisation starting state in the backward direction.

**Steps:**
1. Initialise the synchronisation dialogue (no edits performed).

**Precondition:** none.

**Expected postcondition:** Both models contain only their root `Database` elements (same state as the forward counterpart).
- PDB1: `RootElementPdb1`
- PDB2: `RootElementPdb2`

---

#### `testDatabaseNameChangeOfEmpty`

**What is tested:** Renaming of an empty PDB2 database and backward propagation of the new name to PDB1.

**Steps:**
1. Perform a target edit: set the database name to `"Bundeskanzler"` *(establishes precondition)*.
2. Perform a target edit: rename the database to `"Bundespräsidenten"`.

**Precondition:**
- PDB1 (`EmptyBundeskanzlerPdb1`): empty database, `name="Bundeskanzler"`.
- PDB2 (`EmptyBundeskanzlerPdb2`): empty database, `name="Bundeskanzler"`.

**Expected postcondition:** Both models hold an empty database named `"Bundespräsidenten"`.
- PDB1: `EmptyBundespräsidentenPdb1`
- PDB2: `EmptyBundespräsidentenPdb2`

---

### 3. `BatchBackwardNotFirst` — Batch Backward, Decision = Last Space (PDB2 → PDB1)

Batch tests propagating target-side edits to the source with the decision `PREFER_USING_FIRST_SPACE_TO_LAST = false` (split at the **last** space). Feature tag: **bwd, runtime**.

---

#### `testCreatePerson`

**What is tested:** Creation of a single person (Konrad Adenauer) in PDB2 and backward propagation to PDB1 using the *last-space* splitting strategy.

**Steps:**
1. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
2. Perform a target edit: create `"Konrad Hermann Joseph Adenauer"` in PDB2.

**Precondition:** none (empty models).

**Expected postcondition:**
- PDB1 (`AdenauerPdb1`): `firstName="Konrad Hermann Joseph"`, `lastName="Adenauer"` — all tokens except the last become the first name.
- PDB2 (`AdenauerPdb2`): `name="Konrad Hermann Joseph Adenauer"`.

---

#### `testCreateMultiplePersons`

**What is tested:** Batch creation of the first three chancellors in PDB2 and backward propagation using the *last-space* strategy.

**Steps:**
1. Perform a target edit: set database name to `"Bundeskanzler"` *(establishes precondition)*.
2. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
3. Atomically create Adenauer, Erhard, and Kiesinger in PDB2.

**Precondition:**
- PDB1 (`EmptyBundeskanzlerPdb1`) / PDB2 (`EmptyBundeskanzlerPdb2`): empty database named `"Bundeskanzler"`.

**Expected postcondition:**
- PDB1 (`PDB1FirstThreeChancellors`): persons with multi-word first names and single-word last names (last-space split).
- PDB2 (`Pre_IncrBwdPDB2FirstThreeChancellors`): persons with full concatenated names.

---

### 4. `BatchBackwardFirst` — Batch Backward, Decision = First Space (PDB2 → PDB1)

Batch tests propagating target-side edits to the source with the decision `PREFER_USING_FIRST_SPACE_TO_LAST = true` (split at the **first** space). Feature tag: **bwd, runtime**.

---

#### `testCreatePerson`

**What is tested:** Creation of a single person (Konrad Adenauer) in PDB2 and backward propagation using the *first-space* splitting strategy.

**Steps:**
1. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
2. Perform a target edit: create `"Konrad Hermann Joseph Adenauer"` in PDB2.

**Precondition:** none (empty models).

**Expected postcondition:**
- PDB1 (`AdenauerMultipleLastNamesPdb1`): `firstName="Konrad"`, `lastName="Hermann Joseph Adenauer"` — only the first token becomes the first name.
- PDB2 (`AdenauerPdb2`): `name="Konrad Hermann Joseph Adenauer"`.

This test is the direct counterpart of `BatchBackwardNotFirst.testCreatePerson` and verifies that the two splitting strategies produce different, correct results.

---

#### `testCreateMultiplePersons`

**What is tested:** Batch creation of the first three chancellors in PDB2 and backward propagation using the *first-space* strategy.

**Steps:**
1. Perform a target edit: set database name to `"Bundeskanzler"` *(establishes precondition)*.
2. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
3. Atomically create Adenauer, Erhard, and Kiesinger in PDB2.

**Precondition:**
- PDB1 (`EmptyBundeskanzlerPdb1`) / PDB2 (`EmptyBundeskanzlerPdb2`): empty database named `"Bundeskanzler"`.

**Expected postcondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstThreeChancellorsMultipleLastNames`): persons where only the first token is the first name and all remaining tokens (including the traditional last name) become the last name.
- PDB2 (`Pre_IncrBwdPDB2FirstThreeChancellors`): persons with full concatenated names (unchanged).

---

### 5. `IncrementalForward` — Incremental (Alignment-Based) Forward (PDB1 → PDB2)

Alignment-based (incremental) tests start from an existing synchronised state and apply further source-side edits, verifying that only the delta is propagated and existing correspondences are preserved. Feature tag: **fwd**.

---

#### `testIncrementalInserts`

**What is tested:** Adding two new persons to an existing PDB1 database that already contains six persons and verifying that only the two new persons appear in PDB2 while the six existing ones remain unchanged.

**Steps:**
1. Build the initial state: set database name, then atomically create the first six chancellors in PDB1 (Adenauer, Erhard, Kiesinger, Brandt, Schmidt, Kohl). Propagate forward.
2. Perform an idle target edit to assign `incrementalID` values (simulates alignment metadata).
3. *(Assert precondition: six-chancellor state.)*
4. Perform a source edit: add Gerhard Schröder and Angela Merkel.

**Precondition:**
- PDB1 (`Pre_IncrFwdPDB1FirstSixChancellors`): six persons, `name="Bundeskanzler"`.
- PDB2 (`Pre_IncrFwdPDB2FirstSixChancellors`): six persons with full names and `incrementalID` values.

**Expected postcondition:**
- PDB1 (`IncrFwdPDB1AllChancellors`): eight persons.
- PDB2 (`IncrFwdPDB2AllChancellors`): eight persons, with Schröder and Merkel added, all `incrementalID`s preserved.

---

#### `testIncrementalDeletions`

**What is tested:** Deleting a single person (Kurt Kiesinger) from a six-person PDB1 database and verifying that the corresponding person is also deleted in PDB2.

**Steps:**
1. Build the initial state: create six persons in PDB1. Propagate forward. Apply idle target edit for `incrementalID`.
2. *(Assert precondition: six-chancellor state.)*
3. Perform a source edit: delete Kurt Kiesinger.

**Precondition:**
- PDB1 (`Pre_IncrFwdPDB1FirstSixChancellors`) / PDB2 (`Pre_IncrFwdPDB2FirstSixChancellors`).

**Expected postcondition:**
- PDB1 (`IncrFwdPDB1FirstSixChancellorsWithoutKiesinger`): five persons (Kiesinger removed).
- PDB2 (`IncrFwdPDB2FirstSixChancellorsWithoutKiesinger`): five persons (Kiesinger removed).

---

#### `testIncrementalValueChange`

**What is tested:** Modifying attribute values of several existing persons in PDB1 (one attribute per person, plus all attributes of one person) and verifying the changes are correctly reflected in PDB2.

**Steps:**
1. Build the initial state: six persons in PDB1. Propagate. Apply idle target edit for `incrementalID`.
2. *(Assert precondition: six-chancellor state.)*
3. Perform a source edit with the following simultaneous changes:
   - Change **all attributes** of Helmut Kohl (`changeAllOfHelmutKohl`).
   - Change `birthday` of Kurt Kiesinger (`changeBirthdayOfKurtKiesinger`).
   - Change `firstName` of Konrad Adenauer (`changeFirstNameOfKonradAdenauer`).
   - Change `id` of Helmut Schmidt (`changeIDOfHelmutSchmidt`).
   - Change `lastName` of Ludwig Erhard (`changeLastNameOfLudwigErhard`).
   - Change `placeOfBirth` of Willy Brandt (`changePlaceOfBirthOfWillyBrandt`).

**Precondition:**
- PDB1 (`Pre_IncrFwdPDB1FirstSixChancellors`) / PDB2 (`Pre_IncrFwdPDB2FirstSixChancellors`).

**Expected postcondition:**
- PDB1 (`IncrFwdPDB1FirstSixChancellorsAfterValueChange`): six persons with modified attribute values.
- PDB2 (`IncrFwdPDB2FirstSixChancellorsAfterValueChange`): six persons with the corresponding modified values (name re-concatenated where `firstName`/`lastName` changed).

---

#### `testStability`

**What is tested:** The **stability** property — re-running the forward transformation after an idle source delta (no actual change) must not alter the PDB2 model.

**Steps:**
1. Build the initial state: eight persons in PDB1. Propagate. Apply idle target edit.
2. *(Assert precondition: all-chancellors state.)*
3. Perform an idle source edit (`helperPerson1::idleDelta` — a no-op edit).

**Precondition:**
- PDB1 (`IncrFwdPDB1AllChancellors`): eight persons.
- PDB2 (`IncrFwdPDB2AllChancellorsIDs`): eight persons, all with `incrementalID` values.

**Expected postcondition:** Both models are **unchanged** — identical to the precondition:
- PDB1: `IncrFwdPDB1AllChancellors`
- PDB2: `IncrFwdPDB2AllChancellorsIDs`

---

#### `testHippocraticness`

**What is tested:** The **hippocraticness** property — if a PDB1 model has been created from a PDB2 model in a "wrong" form (with a redundant token in `firstName`), a subsequent source edit that corrects this (`hippocraticDelta`) should not change the PDB2 model (because the concatenated full name is already correct in PDB2).

**Steps:**
1. Build the initial state: create eight persons in PDB1, with Adenauer using a *wrong* split (`createWrongKonradAdenauer`). Propagate. Apply idle target edit.
2. *(Assert precondition: all-chancellors with wrong Adenauer.)*
3. Perform a source edit `hippocraticDelta` that moves the extra name token from `firstName` into `lastName` (or vice versa), resulting in the canonical PDB1 split.

**Precondition:**
- PDB1 (`IncrFwdPDB1AllChancellorsWrongAdenauer`): Adenauer stored with a non-standard split, all others standard.
- PDB2 (`IncrFwdPDB2AllChancellorsIDs`): eight persons with standard full names (the "wrong" split yields the same concatenated name).

**Expected postcondition:**
- PDB1 (`IncrFwdPDB1AllChancellors`): Adenauer now stored with the canonical split.
- PDB2 (`IncrFwdPDB2AllChancellorsIDs`): **unchanged** — hippocraticness confirmed.

---

### 6. `IncrementalBackward` — Incremental (Alignment-Based) Backward (PDB2 → PDB1)

Alignment-based tests propagating target-side edits to PDB1. Feature tag: **bwd**.

---

#### `testIncrementalInsertsFixedConfigLastSpace`

**What is tested:** Adding two new persons to a six-person PDB2 database using a globally fixed *last-space* splitting strategy and verifying that the correct PDB1 entries are created.

**Steps:**
1. Configure globally: `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
2. Atomically create six persons in PDB2 (Adenauer through Kohl) plus set the database name. Propagate backward.
3. Perform an idle source edit (`changeIncrementalIDs`).
4. *(Assert precondition: six-chancellor state — last-space split.)*
5. Perform a target edit: add Gerhard Schröder and Angela Merkel.

**Precondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstSixChancellors`): six persons, last-space split (e.g. `firstName="Konrad Hermann Joseph"`, `lastName="Adenauer"`).
- PDB2 (`Pre_IncrBwdPDB2FirstSixChancellors`): six persons with full names.

**Expected postcondition:**
- PDB1 (`IncrBwdPDB1AllChancellors`): eight persons. The six existing persons retain their `incrementalID` and last-space split. Schröder and Merkel are added with last-space split and empty `incrementalID`.
- PDB2 (`IncrBwdPDB2AllChancellors`): eight persons.

---

#### `testIncrementalInsertsFixedConfigFirstSpace`

**What is tested:** Adding two new persons to a six-person PDB2 database using a globally fixed *first-space* splitting strategy.

**Steps:**
1. Configure globally: `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
2. Atomically create six persons in PDB2 plus set the database name. Propagate backward.
3. Perform idle source edit.
4. *(Assert precondition: six-chancellor state — first-space split.)*
5. Perform a target edit: add Gerhard Schröder and Angela Merkel.

**Precondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstSixChancellorsFirstSpace`): six persons, first-space split (e.g. `firstName="Konrad"`, `lastName="Hermann Joseph Adenauer"`).
- PDB2 (`Pre_IncrBwdPDB2FirstSixChancellors`): six persons with full names.

**Expected postcondition:**
- PDB1 (`IncrBwdPDB1AllChancellorsFirstSpace`): eight persons. Existing six retain their `incrementalID` and first-space split. Schröder and Merkel added with first-space split.
- PDB2 (`IncrBwdPDB2AllChancellors`): eight persons.

---

#### `testIncrementalInsertsDynamicConfig`

**What is tested:** How a **mid-session change of the splitting decision** affects newly added persons. The decision is changed between incremental steps; the test verifies that the new decision is applied only to the new delta and the existing persons retain their original split.

**Steps:**
1. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = false` (last-space).
2. Atomically create three persons (Adenauer, Erhard, Kiesinger) plus set database name. Propagate backward.
3. Perform idle source edit.
4. *(Assert precondition: first three chancellors, last-space split.)*
5. Switch to `PREFER_USING_FIRST_SPACE_TO_LAST = true` (first-space).
6. Add Willy Brandt, Helmut Schmidt, Helmut Kohl to PDB2. Propagate backward.
7. *(Assert intermediate state: first six chancellors; first three last-space, last three first-space.)*
8. Switch back to `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
9. Add Gerhard Schröder. Propagate backward.
10. *(Assert intermediate state: seven persons.)*
11. Switch to `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
12. Add Angela Merkel. Propagate backward.

**Precondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstThreeChancellors`) / PDB2 (`Pre_IncrBwdPDB2FirstThreeChancellors`).

**Expected postconditions (three checkpoints):**
- After step 6: PDB1 (`IncrBwdDynamicConfigPDB1_1`), PDB2 (`Pre_IncrBwdPDB2FirstSixChancellors`).
- After step 9: PDB1 (`IncrBwdDynamicConfigPDB1_2`), PDB2 (`IncrBwdPDB2FirstSevenChancellors`).
- After step 12: PDB1 (`IncrBwdDynamicConfigPDB1_3`), PDB2 (`IncrBwdPDB2AllChancellors`).

---

#### `testIncrementalDeletions`

**What is tested:** Deleting a person (Kurt Kiesinger) from a six-person PDB2 database and verifying the corresponding PDB1 entry is correctly removed.

**Steps:**
1. Atomically create six persons in PDB2 plus set database name. Propagate backward.
2. Perform idle source edit.
3. *(Assert precondition: six-chancellor state.)*
4. Perform a target edit: delete Kurt Kiesinger from PDB2.

**Precondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstSixChancellors`) / PDB2 (`Pre_IncrBwdPDB2FirstSixChancellors`).

**Expected postcondition:**
- PDB1 (`IncrBwdPDB1FirstSixChancellorsWithoutKiesinger`): five persons, Kurt Kiesinger removed.
- PDB2 (`IncrBwdPDB2FirstSixChancellorsWithoutKiesinger`): five persons.

---

#### `testIncrementalValueChange`

**What is tested:** Modifying attribute values of existing persons in PDB2 across two steps with different splitting configurations and verifying the correct reflection in PDB1. The test exercises changing each of the non-name attributes (`birthday`, `id`, `placeOfBirth`) and the full-name attribute (which requires re-splitting) for different persons.

**Steps:**
1. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
2. Create six persons in PDB2 plus set database name. Propagate backward.
3. Perform idle source edit.
4. *(Assert precondition: six-chancellor state, last-space split.)*
5. Perform target edit with simultaneous changes:
   - Change **all** attributes of Helmut Kohl in PDB2 (`changeAllOfHelmutKohl`).
   - Change `birthday` of Kurt Kiesinger (`changeBirthdayOfKurtKiesinger`).
   - Change `firstName`/`name` part for Konrad Adenauer (`changeFirstNameOfKonradAdenauer`).
6. *(Assert intermediate postcondition: `IncrBwdPDB1FirstSixChancellorsAfterValueChange_1` / `IncrBwdPDB2FirstSixChancellorsAfterValueChange_1`.)*
7. Switch to `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
8. Perform target edit with simultaneous changes:
   - Change `id` of Helmut Schmidt (`changeIDOfHelmutSchmidt`).
   - Change `lastName`/`name` part of Ludwig Erhard (`changeLastNameOfLudwigErhard`).
   - Change `placeOfBirth` of Willy Brandt (`changePlaceOfBirthOfWillyBrandt`).

**Precondition:**
- PDB1 (`Pre_IncrBwdPDB1FirstSixChancellors`) / PDB2 (`Pre_IncrBwdPDB2FirstSixChancellors`).

**Expected postconditions (two checkpoints):**
- After step 5: PDB1 (`IncrBwdPDB1FirstSixChancellorsAfterValueChange_1`), PDB2 (`IncrBwdPDB2FirstSixChancellorsAfterValueChange_1`).
- After step 8: PDB1 (`IncrBwdPDB1FirstSixChancellorsAfterValueChange_2`), PDB2 (`IncrBwdPDB2FirstSixChancellorsAfterValueChange_2`).

---

#### `testStability`

**What is tested:** The **stability** property in the backward direction — re-running the backward transformation after an idle target delta must not alter the PDB1 model.

**Steps:**
1. Create all eight persons in PDB2 plus set database name. Propagate backward.
2. Perform idle source edit.
3. *(Assert precondition: all-chancellors state.)*
4. Perform an idle target edit (`helperPerson2::idleDelta`).

**Precondition:**
- PDB1 (`IncrBwdPDB1AllChancellorsIDs`): eight persons.
- PDB2 (`IncrBwdPDB2AllChancellors`): eight persons.

**Expected postcondition:** Both models **unchanged**:
- PDB1: `IncrBwdPDB1AllChancellorsIDs`
- PDB2: `IncrBwdPDB2AllChancellors`

---

#### `testHippocraticness`

**What is tested:** The **hippocraticness** property in the backward direction — re-running the backward transformation with a changed configuration but an otherwise idle target delta must not change the PDB1 model, because the full names in PDB2 are the same and the aligned PDB1 entries already exist.

> **Note (open question documented in source):** The expected behaviour is debated — if the configuration changes, the splitting of existing names in PDB1 might legitimately change. As currently specified, the test asserts that the PDB1 model is *not* altered by the configuration-only change.

**Steps:**
1. Configure: `PREFER_USING_FIRST_SPACE_TO_LAST = false`.
2. Create all eight persons in PDB2 plus set database name. Propagate backward.
3. Perform idle source edit.
4. *(Assert precondition: all-chancellors state, last-space split.)*
5. Switch to `PREFER_USING_FIRST_SPACE_TO_LAST = true`.
6. Perform a hippocratic target delta (`helperPerson2::hippocraticDelta` — a semantically equivalent, no-change edit on the PDB2 side).

**Precondition:**
- PDB1 (`IncrBwdPDB1AllChancellorsIDs`): eight persons, last-space split.
- PDB2 (`IncrBwdPDB2AllChancellors`): eight persons with full names.

**Expected postcondition:** Both models **unchanged**:
- PDB1: `IncrBwdPDB1AllChancellorsIDs`
- PDB2: `IncrBwdPDB2AllChancellors`

---

## Domain Data: German Federal Chancellors

All test persons are the first eight German Federal Chancellors:

| # | Full name | `firstName` (last-space) | `lastName` (last-space) | `birthday` | `placeOfBirth` | `id` |
|---|---|---|---|---|---|---|
| 1 | Konrad Hermann Joseph Adenauer | Konrad Hermann Joseph | Adenauer | 05.01.1876 | Koeln | KA |
| 2 | Ludwig Wilhelm Erhard | Ludwig Wilhelm | Erhard | 04.02.1897 | Fuerth | LE |
| 3 | Kurt Georg Kiesinger | Kurt Georg | Kiesinger | 06.04.1904 | Ebingen | KK |
| 4 | Willy Brandt | Willy | Brandt | 18.12.1913 | Luebeck | WB |
| 5 | Helmut Heinrich Waldemar Schmidt | Helmut Heinrich Waldemar | Schmidt | 23.12.1918 | Hamburg | HS |
| 6 | Helmut Josef Michael Kohl | Helmut Josef Michael | Kohl | 03.04.1930 | Ludwigshafen am Rhein | HK |
| 7 | Gerhard Fritz Kurt Schröder | Gerhard Fritz Kurt | Schroeder | 07.04.1944 | Mossenberg-Woehren | GS |
| 8 | Angela Dorothea Merkel | Angela Dorothea | Merkel | 17.07.1954 | Hamburg | AM |

With the *first-space* splitting strategy (`PREFER_USING_FIRST_SPACE_TO_LAST = true`), only the first token becomes `firstName` and all remaining tokens (including the traditional last name) become `lastName`.

---

## Model State Files

All model state XMI files reside in `resources/`. Names follow a naming convention that encodes which model (PDB1/PDB2), which test direction (fwd/bwd), and which semantic state is represented:

| File | Description |
|---|---|
| `RootElementPdb1.xmi` / `RootElementPdb2.xmi` | Empty root database, no name, no persons |
| `EmptyBundeskanzlerPdb1.xmi` / `...Pdb2.xmi` | Empty database named `"Bundeskanzler"` |
| `EmptyBundespräsidentenPdb1.xmi` / `...Pdb2.xmi` | Empty database renamed to `"Bundespräsidenten"` |
| `AdenauerPdb1.xmi` / `AdenauerPdb2.xmi` | Single person Adenauer (last-space split in PDB1) |
| `AdenauerMultipleLastNamesPdb1.xmi` | Single person Adenauer with first-space split |
| `PDB1FirstThreeChancellors.xmi` | PDB1 with first three chancellors (batch fwd result) |
| `Pre_IncrBwdPDB2FirstThreeChancellors.xmi` | PDB2 precondition: first three chancellors (also batch fwd result) |
| `Pre_IncrBwdPDB1FirstThreeChancellors.xmi` | PDB1 precondition: first three, last-space split |
| `Pre_IncrBwdPDB1FirstThreeChancellorsMultipleLastNames.xmi` | PDB1 precondition: first three, first-space split |
| `Pre_IncrFwdPDB1FirstSixChancellors.xmi` / `Pre_IncrBwdPDB1FirstSixChancellors.xmi` | PDB1 with six persons (precondition for incremental tests) |
| `Pre_IncrFwdPDB2FirstSixChancellors.xmi` / `Pre_IncrBwdPDB2FirstSixChancellors.xmi` | PDB2 with six persons |
| `Pre_IncrBwdPDB1FirstSixChancellorsFirstSpace.xmi` | PDB1 with six persons, first-space split |
| `IncrFwdPDB1AllChancellors.xmi` / `IncrBwdPDB1AllChancellors.xmi` | PDB1 with all eight persons |
| `IncrFwdPDB2AllChancellors.xmi` / `IncrBwdPDB2AllChancellors.xmi` | PDB2 with all eight persons |
| `IncrFwdPDB2AllChancellorsIDs.xmi` / `IncrBwdPDB1AllChancellorsIDs.xmi` | All-chancellors state with `incrementalID` populated |
| `IncrBwdPDB1AllChancellorsFirstSpace.xmi` | PDB1 all-chancellors, first-space split |
| `IncrFwdPDB1AllChancellorsWrongAdenauer.xmi` | PDB1 all-chancellors with Adenauer in non-canonical split |
| `IncrFwdPDB1FirstSixChancellorsWithoutKiesinger.xmi` / `IncrBwdPDB1FirstSixChancellorsWithoutKiesinger.xmi` | PDB1 five persons (Kiesinger deleted) |
| `IncrFwdPDB2FirstSixChancellorsWithoutKiesinger.xmi` / `IncrBwdPDB2FirstSixChancellorsWithoutKiesinger.xmi` | PDB2 five persons |
| `IncrFwdPDB1FirstSixChancellorsAfterValueChange.xmi` | PDB1 six persons after attribute changes (fwd) |
| `IncrFwdPDB2FirstSixChancellorsAfterValueChange.xmi` | PDB2 six persons after attribute changes |
| `IncrBwdPDB1FirstSixChancellorsAfterValueChange_1.xmi` / `_2.xmi` | PDB1 after two successive backward attribute changes |
| `IncrBwdPDB2FirstSixChancellorsAfterValueChange_1.xmi` / `_2.xmi` | PDB2 corresponding states |
| `IncrBwdDynamicConfigPDB1_1.xmi` / `_2.xmi` / `_3.xmi` | Intermediate PDB1 states during dynamic config test |
| `IncrBwdPDB2FirstSevenChancellors.xmi` | PDB2 with seven persons (intermediate dynamic config state) |

## Running the Tests

Because the tests are packaged as a standard Maven project, you can easily run all tests or target a specific tool/test class.

**Run All Tests (for all active tools):**
```sh
cd <workspace-root>
./mvnw test -pl examples/pdb1topdb2/BenchmarxPdb1ToPdb2 -am
```

**Run Tests for a Single Tool:**
To run tests only for a specific tool (e.g., `BXAgentPdb12Pdb2`), supply the `benchmarx.tool` property:
```sh
cd <workspace-root>
./mvnw test -pl examples/pdb1topdb2/BenchmarxPdb1ToPdb2 -am -Dbenchmarx.tool=BXAgentPdb12Pdb2
```

**Run a Single Test Class or Method (for a single tool):**
You can combine the tool filter with Surefire's standard `-Dtest=` parameter. For example, to run only the `BatchForward` class for `BXAgentPdb12Pdb2`:
```sh
./mvnw test -pl examples/pdb1topdb2/BenchmarxPdb1ToPdb2 -am -Dbenchmarx.tool=BXAgentPdb12Pdb2 -Dtest=BatchForward
```
To run a specific method:
```sh
./mvnw test -pl examples/pdb1topdb2/BenchmarxPdb1ToPdb2 -am -Dbenchmarx.tool=BXAgentPdb12Pdb2 -Dtest=BatchForward#testCreateMultiplePersons
```
*(Note: If you omit `-Dbenchmarx.tool=...`, the specified test will be executed against all registered tools).*

---

## Adding a New Tool

To add a new bidirectional transformation tool to this test suite, follow these steps:

1. **Implement the Tool Wrapper**
   Create a new class that implements `org.benchmarx.BXTool<pdb1.Database, pdb2.Database, Decisions>`. This class is responsible for connecting your framework to the Benchmarx execution lifecycle. Place it in the `org.benchmarx.examples.pdb12pdb2.implementations.*` package structure.

2. **Add Dependencies**
   If your tool requires additional libraries, add them to `BenchmarxPdb1ToPdb2/pom.xml`. If the libraries are not available on Maven Central, you must:
   - Place the JARs in the `lib/` folder.
   - Update `install-local-deps.sh` in the workspace root to install those JARs into the local Maven repository.
   - Add the corresponding dependency to `BenchmarxPdb1ToPdb2/pom.xml`.

3. **Register the Tool**
   Open `org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase` and locate the `tools()` method. Register your tool by instantiating it inside the `allTools` list:
   ```java
   List<BXTool<pdb1.Database, pdb2.Database, Decisions>> allTools = Arrays.asList(
           new MyNewToolPdb12Pdb2(), // <-- Add your tool here
           new BXAgentPdb12Pdb2(),
           new BXLangPdb12Pdb2()
   );
   ```

4. **Verify**
   Run the tests for your tool in isolation using the `-Dbenchmarx.tool=MyNewToolPdb12Pdb2` parameter to ensure your implementation passes the Benchmarx properties.
