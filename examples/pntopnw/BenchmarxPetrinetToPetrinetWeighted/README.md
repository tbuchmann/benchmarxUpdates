# BenchmarX – Petrinet to Petrinet Weighted (Pn2Pnw)

This project contains the **BenchmarX test suite** for the bidirectional transformation between
an unweighted Petri net (`pn`) and a weighted Petri net (`pnw`).  
The tests are structured according to the BenchmarX framework: every test follows the
`initialise → (precondition) → edit → postcondition → terminate` protocol and is
parameterised over a list of registered BX tools.

---

## Table of Contents

1. [Transformation Overview](#transformation-overview)
2. [Metamodels](#metamodels)
3. [Named Model States (Resources)](#named-model-states-resources)
4. [Test Classes](#test-classes)
   - [BatchForward](#batchforward)
   - [BatchBackward](#batchbackward)
   - [IncrementalForward](#incrementalforward)
   - [IncrementalBackward](#incrementalbackward)
5. [Registered BX Tools](#registered-bx-tools)

---

## Transformation Overview

The transformation synchronises a **plain Petri net** (source, package `pn`) with a
**weighted Petri net** (target, package `pnw`).  
Both models share the same structural concepts (net, places, transitions, arcs), but the
weighted model additionally carries explicit, typed edge objects (`PTEdge`, `TPEdge`) each
with an integer `weight` attribute.

The key correspondence rules are:

| Source (pn)                       | Target (pnw)                                           |
|-----------------------------------|--------------------------------------------------------|
| `Net` (name, incrementalID)       | `Net` (name, incrementalID)                            |
| `Place` (name, noOfTokens)        | `Place` (name, noOfTokens)                             |
| `Transition` (name)               | `Transition` (name)                                    |
| `Place → Transition` reference    | `PTEdge` (fromPlace, toTransition, weight)             |
| `Transition → Place` reference    | `TPEdge` (fromTransition, toPlace, weight)             |

When a plain arc is created in the source, the tool creates a corresponding `PTEdge` /
`TPEdge` in the target with a default weight of **1**.  
When such a weighted edge already exists in the target, the weight is preserved under
forward propagation (**hippocraticness**).

---

## Metamodels

### Source – `pn` (PetriNet.ecore)

| Concept       | Type       | Key attributes / references                                      |
|---------------|------------|------------------------------------------------------------------|
| `Net`         | EClass     | `name : EString`, `incrementalID : EString`, `elements[]`       |
| `Place`       | EClass     | `name : EString`, `noOfTokens : EInt` (default 1), `trgP2T[]`, `srcT2P[]` |
| `Transition`  | EClass     | `name : EString`, `srcP2T[]` (incoming places), `trgT2P[]` (outgoing places) |

Arcs are represented as **direct cross-references** between `Place` and `Transition`.

### Target – `pnw` (PetriNetWeighted.ecore)

| Concept       | Type       | Key attributes / references                                       |
|---------------|------------|-------------------------------------------------------------------|
| `Net`         | EClass     | `name : EString`, `incrementalID : EString`, `elements[]`        |
| `Place`       | EClass     | `name : EString`, `noOfTokens : EInt` (default 1), `outPTEdges[]`, `inTPEdges[]` |
| `Transition`  | EClass     | `name : EString`, `inPTEdges[]`, `outTPEdges[]`                   |
| `PTEdge`      | EClass     | `weight : EInt` (default 1), `fromPlace`, `toTransition`         |
| `TPEdge`      | EClass     | `weight : EInt` (default 1), `fromTransition`, `toPlace`         |

Arcs are **first-class objects** with an explicit `weight` attribute.

---

## Named Model States (Resources)

All expected model snapshots are stored as XMI files in the `resources/` directory.
Each test references two files: one for the source (`*Pn.xmi`) and one for the target
(`*Pnw.xmi`).  The table below describes every named state.

| Name prefix                              | Source model (`*Pn.xmi`)                                                                                                                                                      | Target model (`*Pnw.xmi`)                                                                                                         |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| `Empty`                                  | Empty net, no name, no elements.                                                                                                                                              | Empty weighted net, no name, no elements.                                                                                        |
| `EmptyLettersDigits`                     | Empty net named **"LettersAndDigits"**.                                                                                                                                        | Empty weighted net named **"LettersAndDigits"**.                                                                                  |
| `EmptyFactoryModel`                      | Empty net named **"FactoryModel"**.                                                                                                                                            | Empty weighted net named **"FactoryModel"**.                                                                                     |
| `SimpleLettersDigits`                    | Net "LettersAndDigits" with 7 places (A–G, tokens 0/1/1/2/3/5/8) and 4 transitions (1–4) with various arcs.                                                                  | Same structure with explicit `PTEdge`/`TPEdge` objects; default weights of 1 throughout (no custom weights).                     |
| `SimpleLettersDigitsWeighted`            | Same as `SimpleLettersDigits` source.                                                                                                                                         | Same as `SimpleLettersDigits` target, but with Fibonacci-style weights: T1 PT=13/TP=0, T2 PT=0/TP=21, T3 PT=34/TP=55, T4 PT=89/TP=144. |
| `ComplexLettersDigits`                   | Net "LettersAndDigits" with 4 places (A=9, B=16, C=25, D=36) and 4 transitions (1–4) with multi-source/multi-target arcs.                                                    | Same structure with explicit edges; default weight 1.                                                                            |
| `ComplexLettersDigitsWeighted`           | Same as `ComplexLettersDigits` source.                                                                                                                                        | Same as `ComplexLettersDigits` target with non-default edge weights (various).                                                   |
| `ComplexLettersDigitsSimpleWeighted`     | Same as `ComplexLettersDigits` source.                                                                                                                                        | Same structure with only one non-default weight: PT-edge A→3 weight=2, TP-edge 3→D weight=4.                                    |
| `ComplexLettersDigitsChanged`            | Same topology as `ComplexLettersDigits` but with `incrementalID` set to `"changed"`.                                                                                          | (used only as source state, paired with corresponding Pnw snapshot).                                                            |
| `PTPLettersDigits`                       | Net "LettersAndDigits" with 2 places (A=1, B=0) and 1 transition (1: A→1→B).                                                                                                 | Same structure with explicit edges; default weight 1 on both edges.                                                              |
| `PTPLettersDigitsWeighted`               | Same as `PTPLettersDigits` source.                                                                                                                                            | Same structure with custom weights: PT-edge A→1 weight=4, TP-edge 1→B weight=2.                                                 |
| `PTPLettersDigitsWeighted73`             | Same as `PTPLettersDigits` source.                                                                                                                                            | Same structure with updated custom weights: PT-edge A→1 weight=7, TP-edge 1→B weight=3.                                         |
| `PTPLettersDigitsChanged`                | Same as `PTPLettersDigits` source but with `incrementalID = "changed"`.                                                                                                       | (used only as source state).                                                                                                     |
| `PTPExtendedLettersDigits`               | `PTPLettersDigits` extended: place C=0 added; transition 1 now also consumes from C; new transition 2 (A→2→B).                                                               | Corresponding weighted structure; default weights.                                                                               |
| `PTPExtendedLettersDigitsWeighted`       | Same as `PTPExtendedLettersDigits` source.                                                                                                                                    | Same with custom weight PT-edge A→1 weight=4, TP-edge 1→B weight=2.                                                             |
| `PTPExtendedLettersDigitsChanged`        | Same as `PTPExtendedLettersDigits` but with `incrementalID = "changed"`.                                                                                                      | (used only as source state).                                                                                                     |
| `PTPFurtherExtendedLettersDigits`        | `PTPExtendedLettersDigits` further extended: transition 1 additionally consumes from B; transition 2 additionally produces to C.                                               | Corresponding weighted structure; default weights.                                                                               |
| `PTPFurtherExtendedLettersDigitsWeighted`| Same as `PTPFurtherExtendedLettersDigits` source.                                                                                                                             | Same with custom weight PT-edge A→1 weight=4, TP-edge 1→B weight=2.                                                             |
| `PTPFurtherExtendedLettersDigitsChanged` | Same as `PTPFurtherExtendedLettersDigits` but with `incrementalID = "changed"`.                                                                                               | (used only as source state).                                                                                                     |
| `1234LettersDigits`                      | Net "LettersAndDigits" with 4 places (A=1, B=2, C=3, D=4) and 2 transitions (1: A→B; 2: B+C→D).                                                                             | Corresponding weighted structure; custom weights: A→1 weight=1, 1→B weight=1, B→2+C→2 weight=1, 2→D weight=1.                  |
| `1234LettersDigitsWeighted`              | Same as `1234LettersDigits` source.                                                                                                                                           | Same with B→2 weight=9.                                                                                                          |
| `1234LettersDigitsChanged`               | Same as `1234LettersDigits` but with `incrementalID = "changed"`.                                                                                                             | (used as source state in backward incremental tests).                                                                            |
| `5678LettersDigits`                      | Derived from `1234LettersDigits` by changing tokens (5,6,7,8), restructuring arcs, and renaming A↔D and renaming B→E.                                                        | Corresponding weighted structure.                                                                                                |
| `5678LettersDigitsWeighted`              | Same as `5678LettersDigits` source.                                                                                                                                           | Same with preserved weights after reconnection.                                                                                  |
| `9012LettersDigits`                      | Derived from `1234LettersDigits` by changing tokens (9,0,1,2), reconnecting edges, renaming places and transitions.                                                           | (used only as source state in backward test).                                                                                    |
| `9012LettersDigitsWeighted`              | Same as `9012LettersDigits` source.                                                                                                                                           | Corresponding weighted structure after backward propagation of the `construct9012` edit.                                         |

---

## Test Classes

All four test classes extend `Pn2PnwTestCase` and use JUnit 5 parameterized tests.
Each test receives a `BXTool` instance provided by `BXToolParameterResolver` / `@MethodSource("tools")`.

### BatchForward

**Package:** `org.benchmarx.examples.pn2pnw.testsuite.batch.fwd`  
**Direction:** forward (source → target, `performAndPropagateSourceEdit`)  
All tests start from scratch (no prior synchronisation state); the entire model is created
in a single step.

#### `testInitialiseSynchronisation`

- **What is tested:** The agreed-upon initial synchronisation state produced by calling
  `initiateSynchronisationDialogue()` alone, without any further edits.
- **Setup:** None. The tool is initialised and no edit is applied.
- **Expected postcondition:** Both source and target contain an empty, unnamed root `Net`
  object (`EmptyPn` / `EmptyPnw`).
- **Features:** fwd, fixed

---

#### `testNetNameChangeOfEmpty`

- **What is tested:** That renaming an empty source net is correctly propagated to the
  target net.
- **Setup:** The source net is renamed to `"LettersAndDigits"` and the target is
  expected to reflect this (`EmptyLettersDigitsPn` / `EmptyLettersDigitsPnw`).
- **Edit:** The source net is renamed to `"FactoryModel"`.
- **Expected postcondition:** Both nets carry the name `"FactoryModel"`
  (`EmptyFactoryModelPn` / `EmptyFactoryModelPnw`).
- **Features:** fwd, fixed

---

#### `testCreateSimpleNet`

- **What is tested:** Batch-forward creation of a simple net with 7 places and 4
  transitions.
- **Setup:** None.
- **Edit:** The full `SimpleLettersDigits` source model is created in one step
  (places A–G with tokens 0/1/1/2/3/5/8; transitions 1–4 with various arcs).
- **Expected postcondition:** The source matches `SimpleLettersDigitsPn`; the target
  matches `SimpleLettersDigitsPnw` (all edges with default weight 1).
- **Features:** fwd, fixed

---

#### `testCreateComplexNet`

- **What is tested:** Batch-forward creation of a more complex net with multi-source
  and multi-target transitions.
- **Setup:** None.
- **Edit:** The full `ComplexLettersDigits` source model is created in one step
  (places A=9, B=16, C=25, D=36; transitions 1–4 with multi-source/multi-target arcs).
- **Expected postcondition:** The source matches `ComplexLettersDigitsPn`; the target
  matches `ComplexLettersDigitsPnw` (all edges with default weight 1).
- **Features:** fwd, fixed

---

### BatchBackward

**Package:** `org.benchmarx.examples.pn2pnw.testsuite.batch.bwd`  
**Direction:** backward (target → source, `performAndPropagateTargetEdit`)  
All tests start from scratch; the entire model is created by editing the target.

#### `testNetNameChangeOfEmpty`

- **What is tested:** That renaming an empty target net is correctly back-propagated to
  the source net.
- **Setup:** The target net is renamed to `"LettersAndDigits"` and checked
  (`EmptyLettersDigitsPn` / `EmptyLettersDigitsPnw`).
- **Edit:** The target net is renamed to `"FactoryModel"`.
- **Expected postcondition:** Both nets carry the name `"FactoryModel"`
  (`EmptyFactoryModelPn` / `EmptyFactoryModelPnw`).
- **Features:** bwd, fixed

---

#### `testCreateSimpleNet`

- **What is tested:** Batch-backward creation of a simple weighted net with default
  weights and back-propagation of its unweighted counterpart.
- **Setup:** None.
- **Edit:** The full `SimpleLettersDigits` target model is created in one step
  (places A–G with tokens 0/1/1/2/3/5/8; transitions 1–4; explicit `PTEdge`/`TPEdge`
  with Fibonacci-style weights 13, 21, 34, 55, 89, 144).
- **Expected postcondition:** The source matches `SimpleLettersDigitsPn`; the target
  matches `SimpleLettersDigitsWeightedPnw`.
- **Features:** bwd, fixed

---

#### `testCreateComplexNet`

- **What is tested:** Batch-backward creation of a complex weighted net and
  back-propagation to the unweighted source.
- **Setup:** None.
- **Edit:** The full `ComplexLettersDigits` target model is created in one step with
  multi-source/multi-target transitions and explicit edge weights.
- **Expected postcondition:** The source matches `ComplexLettersDigitsPn`; the target
  matches `ComplexLettersDigitsWeightedPnw`.
- **Features:** bwd, fixed

---

### IncrementalForward

**Package:** `org.benchmarx.examples.pn2pnw.testsuite.alignment_based.fwd`  
**Direction:** forward (source → target, `performAndPropagateSourceEdit`)  
These tests use an established synchronisation state as a starting point. The target may
have been modified (idle edit) to set custom weights before the forward propagation.

#### `testIncrementalInserts`

- **What is tested:** That adding a place and additional transitions into an already
  synchronised net is correctly propagated to the target while existing weighted edges
  are preserved.
- **Setup:** The source `PTPLettersDigits` (places A=1, B=0; transition 1: A→B) is
  created and propagated; the target weight A→1 is set to 4, 1→B to 2 (idle target
  edit) → `PTPLettersDigitsPn` / `PTPLettersDigitsWeightedPnw`.
- **Edit:** Place C=0 is added; transition 1 gains an additional source arc from C;
  new transition 2 (A→B) is added to the source.
- **Expected postcondition:** Source matches `PTPExtendedLettersDigitsPn`; target
  matches `PTPExtendedLettersDigitsWeightedPnw` (new edges with default weight 1,
  existing edge weights preserved).
- **Features:** fwd, add, fixed

---

#### `testIncrementalDeletions`

- **What is tested:** That removing a place and a transition from an already
  synchronised net is correctly propagated to the target.
- **Setup:** The source `PTPExtendedLettersDigits` is created and propagated; the
  target weight A→1 is set to 4, 1→B to 2 → `PTPExtendedLettersDigitsPn` /
  `PTPExtendedLettersDigitsWeightedPnw`.
- **Edit:** Place C and transition 2 are deleted from the source.
- **Expected postcondition:** Source matches `PTPLettersDigitsPn`; target matches
  `PTPLettersDigitsWeightedPnw` (existing weighted edges preserved).
- **Features:** fwd, del, corr-based, structural

---

#### `testIncrementalChanges`

- **What is tested:** That changing token counts, restructuring arcs, and renaming
  elements in an already synchronised source net is correctly propagated to the target.
- **Setup:** Source `1234LettersDigits` (places A=1, B=2, C=3, D=4; transitions 1
  and 2) is created and propagated; target weights B→2 set to 9 (idle edit) →
  `1234LettersDigitsPn` / `1234LettersDigitsWeightedPnw`.
- **Edit:** Tokens of A–D are changed to 5–8; arcs are restructured; places A and D
  are swapped; place B renamed to E; transitions 1 and 2 are swapped.
- **Expected postcondition:** Source matches `5678LettersDigitsPn`; target matches
  `5678LettersDigitsWeightedPnw`.
- **Features:** fwd, attribute, fixed, structural, corr-based

---

#### `testStability`

- **What is tested:** That propagating an **idle source edit** (a source delta that
  changes nothing) does not modify the target model (*stability*).
- **Setup:** Source `ComplexLettersDigits` is created and propagated; target weight
  A→3 set to 2, 3→D set to 4 → `ComplexLettersDigitsPn` /
  `ComplexLettersDigitsSimpleWeightedPnw`.
- **Edit:** An empty/idle source delta is propagated.
- **Expected postcondition:** Both models remain unchanged: `ComplexLettersDigitsPn` /
  `ComplexLettersDigitsSimpleWeightedPnw`.
- **Features:** fwd, fixed

---

#### `testHipporcraticness`

- **What is tested:** That propagating a source edit which changes only the
  `incrementalID` attribute (a source-only attribute with no counterpart in the target)
  does **not** alter the target model (*hippocraticness*).
- **Setup:** Source `ComplexLettersDigits` is created and propagated; target weight
  A→3 set to 2, 3→D set to 4 → `ComplexLettersDigitsPn` /
  `ComplexLettersDigitsSimpleWeightedPnw`.
- **Edit:** The source `incrementalID` is set to `"changed"`.
- **Expected postcondition:** Source matches `ComplexLettersDigitsChangedPn`
  (incrementalID changed); target remains `ComplexLettersDigitsSimpleWeightedPnw`
  (unchanged).
- **Features:** fwd, fixed

---

### IncrementalBackward

**Package:** `org.benchmarx.examples.pn2pnw.testsuite.alignment_based.bwd`  
**Direction:** backward (target → source, `performAndPropagateTargetEdit`)  
These tests use an established synchronisation state as a starting point. The source may
have been modified (idle edit) to set the `incrementalID` before backward propagation.

#### `testIncrementalInserts`

- **What is tested:** That adding a place, transitions and edges to an already
  synchronised weighted target net is correctly back-propagated to the source while
  keeping existing elements unchanged.
- **Setup:** Target `PTPLettersDigitsWeighted` (place A=1, B=0; transition 1 with
  PT-edge weight 4 and TP-edge weight 2) is created and propagated; source
  `incrementalID` is set to `"changed"` (idle source edit) →
  `PTPLettersDigitsChangedPn` / `PTPLettersDigitsWeightedPnw`.
- **Edit 1:** Place C=0 added; transition 1 gains source arc from C; transition 2
  (A→B) added in the target.
- **Expected postcondition 1:** `PTPExtendedLettersDigitsChangedPn` /
  `PTPExtendedLettersDigitsWeightedPnw`.
- **Edit 2:** Transition 1 gains an additional source from B; transition 2 gains an
  additional target to C.
- **Expected postcondition 2:** `PTPFurtherExtendedLettersDigitsChangedPn` /
  `PTPFurtherExtendedLettersDigitsWeightedPnw`.
- **Features:** bwd, add, fixed

---

#### `testIncrementalDeletions`

- **What is tested:** That removing a place and transitions from an already
  synchronised weighted target net is correctly back-propagated to the source.
- **Setup:** Target `PTPFurtherExtendedLettersDigitsWeighted` with custom weight is
  created and propagated; source `incrementalID` set to `"changed"` →
  `PTPFurtherExtendedLettersDigitsChangedPn` /
  `PTPFurtherExtendedLettersDigitsWeightedPnw`.
- **Edit 1:** Source arcs from B and C are removed from transitions 1 and 2
  respectively, reducing to the extended state.
- **Expected postcondition 1:** `PTPExtendedLettersDigitsChangedPn` /
  `PTPExtendedLettersDigitsWeightedPnw`.
- **Edit 2:** Place C and transition 2 are deleted from the target.
- **Expected postcondition 2:** `PTPLettersDigitsChangedPn` /
  `PTPLettersDigitsWeightedPnw`.
- **Features:** bwd, del, corr-based, structural

---

#### `testIncrementalChanges`

- **What is tested:** That changing token counts, reconnecting weighted edges, and
  renaming places and transitions in the weighted target net is correctly
  back-propagated to the source.
- **Setup:** Target `1234LettersDigitsWeighted` (A=1, B=2, C=3, D=4; transitions 1
  and 2 with B→2 weight=9) is created and propagated; source `incrementalID` set to
  `"changed"` → `1234LettersDigitsChangedPn` / `1234LettersDigitsWeightedPnw`.
- **Idle target edit:** PT-edge A→1 weight changed to 7 and TP-edge 1→B weight
  changed to 3 (this edit is applied idly and does **not** trigger propagation).
- **Edit:** Tokens of A–D changed to 9, 0, 1, 2; PT-edge B→2 reconnected to
  D→1; TP-edge 2→D reconnected to 2→A; places A and D swapped; B renamed to E;
  transitions 1 and 2 swapped.
- **Expected postcondition:** `9012LettersDigitsChangedPn` /
  `9012LettersDigitsWeightedPnw`.
- **Features:** bwd, attribute, fixed, structural, corr-based

---

#### `testStability`

- **What is tested:** That propagating an **idle target edit** (a target delta that
  changes nothing) does not modify the source model (*stability*).
- **Setup:** Target `ComplexLettersDigitsWeighted` is created and propagated; source
  `incrementalID` set to `"changed"` → `ComplexLettersDigitsChangedPn` /
  `ComplexLettersDigitsWeightedPnw`.
- **Edit:** An empty/idle target delta is propagated.
- **Expected postcondition:** Both models remain unchanged:
  `ComplexLettersDigitsChangedPn` / `ComplexLettersDigitsWeightedPnw`.
- **Features:** bwd, fixed

---

#### `testHippocraticness`

- **What is tested:** That propagating a target edit which changes only a weighted
  edge (a target-specific attribute not mirrored in the source) does **not** alter the
  source model (*hippocraticness*).
- **Setup:** Target `PTPLettersDigitsWeighted` (PT-edge A→1 weight=4, TP-edge 1→B
  weight=2) is created and propagated; source `incrementalID` set to `"changed"` →
  `PTPLettersDigitsChangedPn` / `PTPLettersDigitsWeightedPnw`.
- **Edit:** PT-edge A→1 weight changed to 7 and TP-edge 1→B weight changed to 3 in
  the target.
- **Expected postcondition:** Source remains `PTPLettersDigitsChangedPn` (unchanged);
  target becomes `PTPLettersDigitsWeighted73Pnw`.
- **Features:** bwd, fixed

---

## Registered BX Tools

The active tool list is configured in `Pn2PnwTestCase.tools()`.  
Currently registered (others are commented out):

| Tool           | Class                                                                      | Description                                   |
|----------------|----------------------------------------------------------------------------|-----------------------------------------------|
| **BXAgent**    | `BXAgentPn2Pnw` (`implementations.bxagent`)                               | Agent-based BX implementation using EMT Agent |

The following tools are available but currently disabled:

| Tool           | Class                                                                      |
|----------------|----------------------------------------------------------------------------|
| BXtend         | `BXtendPn2Pnw` (`implementations.bxtend`)                                 |
| MediniQVT      | `MediniQVTPn2Pnw` (`implementations.medini`)                               |
| BXLang         | `BXLangPn2Pnw` (`implementations.bxlang`)                                 |
| IBeXTGG        | `IBeXTGGPetrinets` (`implementations.ibextgg`)                             |

To activate a tool, uncomment the corresponding line in `Pn2PnwTestCase.tools()`.
