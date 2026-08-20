# BenchmarxAstToDag — Test Suite Documentation

## Overview

This project is part of the **Benchmarx** benchmark framework for bidirectional model transformations (BX). It tests the correctness of implementations that synchronise a **source model** (an arithmetic expression tree — AST) with a **target model** (an arithmetic expression DAG — Directed Acyclic Graph). The key characteristic of the transformation is that structurally identical sub-expressions in the AST are **shared** (deduplicated) into a single node in the DAG, while the DAG preserves explicit back-references (`leftInverse`, `rightInverse`) to all operators that use a shared node.

Both the forward (AST → DAG) and backward (DAG → AST) directions are tested, in both **batch** (from empty) and **incremental** (alignment-based) modes.

---

## Metamodels

### ExpressionAST (source, `ast` package)

The AST metamodel represents an arithmetic expression as a **strict binary tree**:

| Class | Description |
|---|---|
| `Model` | Root container; holds exactly one `expr` (the root expression) |
| `Expression` (abstract) | Base class; carries an optional `incrementalID : String` attribute and back-references `leftInverse`/`rightInverse` to at most one parent `Operator` |
| `Operator` | Binary operator node; has a `left` child, a `right` child, and an `op : ArithmeticOperator` attribute |
| `Operand` (abstract) | Leaf base class |
| `Variable` | Leaf carrying a string `name` attribute |
| `Number` | Leaf carrying an integer `value` attribute |
| `ArithmeticOperator` | Enum: `Add`, `Subtract`, `Multiply`, `Divide` |

Because `Operator.left` and `Operator.right` are **containment** references, each `Expression` node has exactly one parent → the AST is a tree.

### ExpressionDAG (target, `dag` package)

The DAG metamodel is structurally identical to the AST metamodel, with one crucial difference: the `left` and `right` references on `Operator` are **non-containment** cross-references, and their inverses (`leftInverse`, `rightInverse`) are **multi-valued** (upper bound `*`). This allows a single `Expression` node to be referenced by multiple `Operator` parents, turning the model into a DAG. All nodes are owned in a flat `exprs` list on `Model`.

---

## Transformation Semantics (AST ↔ DAG)

**Forward (AST → DAG):** Every node in the AST tree is mapped to a node in the DAG. Sub-trees whose root nodes are structurally identical (same type, same attribute values, same sub-structure) are **deduplicated**: they map to the **same** DAG node, which is referenced from multiple parent operators via the non-containment `left`/`right` references.

**Backward (DAG → AST):** Every node in the flat DAG `exprs` list is expanded back into a tree. Nodes referenced from multiple parent operators in the DAG produce **duplicate sub-trees** in the AST.

**Incremental alignment:** The `incrementalID` attribute on `Expression` is used as a stable identity key to align source and target elements across incremental edits, enabling the synchroniser to identify which existing elements correspond to which, and apply only the minimum necessary changes.

---

## Project Structure

```
BenchmarxAstToDag/
├── src/
│   ├── org/benchmarx/examples/ast2dag/
│   │   ├── testsuite/
│   │   │   ├── Ast2DagTestCase.java          — abstract base class for all tests
│   │   │   ├── Decisions.java                — empty enum (no policy decisions required)
│   │   │   ├── BXToolParameterResolver.java  — JUnit parameter resolver
│   │   │   ├── batch/
│   │   │   │   ├── fwd/BatchForward.java     — batch forward tests
│   │   │   │   └── bwd/BatchBackward.java    — batch backward tests
│   │   │   └── alignment_based/
│   │   │       ├── fwd/IncrementalForward.java  — incremental forward tests
│   │   │       └── bwd/IncrementalBackward.java — incremental backward tests
│   │   └── implementations/
│   │       ├── bxlang/BXLangAst2Dag.java     — BXLang tool adapter
│   │       ├── bxagent/BXAgentAst2Dag.java   — BXAgent tool adapter
│   │       ├── bxtend/BXtendAst2Dag.java     — BXtend adapter (disabled)
│   │       └── medini/MediniQVTAst2Dag.java  — MediniQVT adapter (disabled)
├── resources/                               — expected model states (XMI)
├── samples/                                 — sample models
└── ast_dag.corr.xmi                         — example correspondence model
```

---

## Tool Implementations Under Test

All four test classes are **parameterised**: the same tests run against every registered tool implementation. Currently active:

| Tool name | Class | Description |
|---|---|---|
| `BXLang` | `BXLangAst2Dag` | Transformation implemented in BXLang; uses `ExprTree2ExprDAGTransformation` generated from a BXLang specification |
| `BXAgent` | `BXAgentAst2Dag` | Transformation implemented with the BXAgent framework; uses `Ast2DagTransformation` together with a `CorrespondenceModel` for incremental synchronisation |

---

## Test Infrastructure

### `Ast2DagTestCase` (abstract base class)

Sets up the complete test environment before each test:

- Registers the `ast` and `dag` EMF packages.
- Creates `AstComparator` and `DagComparator` instances for model comparison.
- Calls `tool.initiateSynchronisationDialogue()` to establish the initial consistent state (empty AST model → empty DAG model).
- Creates `AstHelper` and `DagHelper` instances that record model-editing operations as typed edit objects (`CreateNode`, `CreateEdge`, `ChangeAttribute`, `DeleteNode`, `DeleteEdge`, `MoveNode`).
- Tears down via `tool.terminateSynchronisationDialogue()` after each test.

Helper factory methods `srcEdit(Runnable...)` and `trgEdit(Runnable...)` wrap model-editing calls into `IEdit<M>` suppliers that can be passed directly to the tool.

### `AstHelper`

Provides high-level named edit operations on the AST model:

| Method | Operation |
|---|---|
| `create42()` | Creates a single `Number` node with value `42` as the root expression |
| `createTextSum()` | Creates an `Add(Add(Variable("Answer to the Ultimate Question..."), Variable("Deep Thought")), Variable("7.5 million years"))` tree — an homage to The Hitchhiker's Guide |
| `createComplexNumberExample()` | Creates the tree `Add(Subtract(Multiply(Add(10,1), Divide(10,5)), 1), Subtract(Multiply(Divide(10,5), Add(1,10)), 1))` — a complex arithmetic expression where `Divide(10,5)` appears as a shared subtree in the resulting DAG |
| `createMulitpleSubtrees()` | Creates a large tree with deeply nested `Add` operators built from `Number(1)` leaves; many structurally identical sub-trees are present |
| `createBestDigit()` | Creates `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))` — the "best digit" expression where the two `Number(7)` nodes share a single DAG node |
| `insertMoreBestDigits()` | Extends `BestDigit` by replacing the right `Number(7)` leaf with `Subtract(Multiply(Number(7), Variable("sieben")), Variable("zweiundvierzig"))`, creating a deeper nested expression |
| `createMoreBestDigits()` | Executes `createBestDigit()` followed by `insertMoreBestDigits()` |
| `removeSomeBestDigits()` | Reduces `MoreBestDigits` back to `BestDigit` by deleting the right sub-tree and replacing with a single `Number(7)` leaf |
| `createBestDigitRef()` | Creates `Add(Subtract(Multiply(Number(7), Variable("sieben")), Number(7)), Multiply(Subtract(Number(14), Number(14)), Divide(Number(7), Number(2))))` — an expression with two occurrences of `Number(7)` that map to the same DAG node |
| `modifyBestDigitRef()` | Modifies `BestDigitRef`: changes the Subtract's left child's Multiply into `Multiply(Subtract(Number(8), Variable("zwei")), Number(7))` and the right Divide into `Multiply(Number(14), Number(14))` |
| `createSimpleASTRef()` | Creates `Multiply(Add(Variable("a"), Number(5)), Add(Variable("a"), Number(7)))` — two occurrences of `Variable("a")` that become a single shared node in the DAG |
| `modifySimpleASTRef()` | Changes the `Number(7)` in the right sub-tree to `Number(5)`, causing the two `Add` sub-trees to become structurally identical and share the same DAG node |
| `changeIncrementalID()` | Sets `incrementalID = "incrTestValue"` on all `Expression` nodes in the model — used to establish alignment identity before incremental edits |
| `idleDelta()` | No-op; represents an idle (empty) edit used to test stability |

### `DagHelper`

Mirrors `AstHelper` but operates on the DAG model. Key differences:

- Node creation uses `saveReference(name)` / `reference(direction, name)` to build cross-references (shared nodes) rather than containment trees.
- `createComplexNumberExample()` shares `Number(10)`, `Number(1)`, and the `Divide(10,5)` operator between two sub-DAGs.
- `createMulitpleSubtrees()` shares a `Number(1)` node across all leaf positions.
- `createBestDigit()` shares `Number(7)` between the `Multiply` left child and the root `Subtract` right child.
- `modifyBestDigitRef()` replaces nodes and updates shared references accordingly.
- `changeIncrementalID()` sets `incrementalID = "incrTestValue"` on all DAG `Expression` nodes.
- `changeIncrementalIDOf8()` sets `incrementalID = "incrTestValue8"` specifically on `Number(8)` nodes.
- `idleDelta()` is a no-op used in stability tests.

---

## Test Classes and Individual Tests

### `BatchForward` — Batch forward propagation (AST → DAG)

These tests start from an **empty, consistent** state (empty AST root, empty DAG root) and apply a single source edit, then verify both models.

---

#### `testInitialiseSynchronisation`

**Purpose:** Verifies that the agreed-upon starting state of the synchronisation dialogue is correctly established — both models contain their respective empty root elements.

**Precondition:** None (empty state directly after `initiateSynchronisationDialogue`).

**Edit:** None.

**Expected AST postcondition (`RootElementAst`):**
```
Model  (empty root, no expr)
```

**Expected DAG postcondition (`RootElementDag`):**
```
Model  (empty root, no exprs)
```

---

#### `testCreateSingleExpression`

**Purpose:** Tests forward propagation of a single-node expression.

**Precondition:** None (empty models).

**Edit (source):** `helperAst.create42()` — creates `Number(42)` as the root expression of the AST.

**Expected AST postcondition (`42Ast`):**
```
Model
  expr: Number(value=42)
```

**Expected DAG postcondition (`42Dag`):**
```
Model
  exprs[0]: Number(value=42)
```

---

#### `testCreateMultipleExpressions`

**Purpose:** Tests forward propagation of a multi-level tree with no shared sub-expressions.

**Precondition:** None (empty models).

**Edit (source):** `helperAst.createTextSum()` — creates:
```
Operator(Add)
  left: Operator(Add)
    left: Variable(name="Answer to the Ultimate Question of Life, The Universe, and Everything")
    right: Variable(name="Deep Thought")
  right: Variable(name="7.5 million years")
```

**Expected AST postcondition (`HG2GAst`):** The above tree.

**Expected DAG postcondition (`HG2GDag`):** A flat DAG with 5 nodes; no sharing occurs because all leaf nodes have distinct names:
```
Model
  exprs[0]: Operator(Add, left→exprs[1], right→exprs[4])
  exprs[1]: Operator(Add, leftInverse←exprs[0], left→exprs[2], right→exprs[3])
  exprs[2]: Variable(name="Answer to the Ultimate Question of Life, The Universe, and Everything", leftInverse←exprs[1])
  exprs[3]: Variable(name="Deep Thought", rightInverse←exprs[1])
  exprs[4]: Variable(name="7.5 million years", rightInverse←exprs[0])
```

---

#### `testCreateMultipleExpressionsComplex`

**Purpose:** Tests forward propagation of a complex expression where structural sharing (deduplication) occurs.

**Precondition:** None (empty models).

**Edit (source):** `helperAst.createComplexNumberExample()` — creates a large tree of the form:
```
Add(
  Subtract(
    Multiply(
      Add(Number(10), Number(1)),
      Divide(Number(10), Number(5))
    ),
    Number(1)
  ),
  Subtract(
    Multiply(
      Divide(Number(10), Number(5)),
      Add(Number(1), Number(10))
    ),
    Number(1)
  )
)
```

**Expected AST postcondition (`42ByMultiplyAddSubtractDivideAst`):** The full tree above (no sharing in AST).

**Expected DAG postcondition (`42ByMultiplyAddSubtractDivideDag`):** A DAG with 9 nodes; the nodes `Number(10)`, `Number(1)`, `Number(5)`, and `Divide(10,5)` appear as shared nodes referenced from multiple operator parents.

---

#### `testCreateMultipleSameSubtrees`

**Purpose:** Tests forward propagation of a large AST with many identical sub-trees, resulting in aggressive structural sharing in the DAG.

**Precondition:** None (empty models).

**Edit (source):** `helperAst.createMulitpleSubtrees()` — creates a deeply nested tree built entirely from `Number(1)` leaves, with the structure:
```
Subtract(
  Multiply(
    <large Add-subtree with only Number(1) leaves>,
    <identical Add-subtree>
  ),
  Divide(
    <identical Add-subtree>,
    <Add(Number(1), Number(1))>
  )
)
```

**Expected AST postcondition (`42ByMultipleSubteesAst`):** The full tree as generated (no sharing in AST).

**Expected DAG postcondition (`42ByMultipleSubtreesDag`):** A highly shared DAG with only 9 nodes (all identical sub-trees deduplicated), where the single `Number(1)` node, several `Add(1,1)` and larger `Add`-subtrees are shared widely.

---

### `BatchBackward` — Batch backward propagation (DAG → AST)

These tests start from an **empty, consistent** state and apply a single **target** edit to the DAG, then verify both models.

---

#### `testCreateSingleExpression`

**Purpose:** Tests backward propagation of a single-number DAG to an AST.

**Precondition:** None.

**Edit (target):** `helperDag.create42()` — creates `Number(42)` in the DAG.

**Expected AST postcondition (`42Ast`):** Same as `BatchForward.testCreateSingleExpression`.

**Expected DAG postcondition (`42Dag`):** Same as `BatchForward.testCreateSingleExpression`.

---

#### `testCreateMultipleExpressions`

**Purpose:** Tests backward propagation of a multi-node DAG (no sharing) to an AST tree.

**Precondition:** None.

**Edit (target):** `helperDag.createTextSum()` — builds the same 5-node Hitchhiker's Guide sum in the DAG directly.

**Expected postconditions (`HG2GAst`, `HG2GDag`):** Identical to `BatchForward.testCreateMultipleExpressions`.

---

#### `testCreateMultipleExpressionsComplex`

**Purpose:** Tests backward propagation of a complex DAG with shared nodes, producing a tree AST with duplicated sub-trees.

**Precondition:** None.

**Edit (target):** `helperDag.createComplexNumberExample()` — builds the complex 9-node DAG directly, using saved references to share `Number(10)`, `Number(1)`, `Number(5)`, and `Divide(10,5)`.

**Expected postconditions (`42ByMultiplyAddSubtractDivideAst`, `42ByMultiplyAddSubtractDivideDag`):** Identical to `BatchForward.testCreateMultipleExpressionsComplex`.

---

#### `testCreateMultipleSameSubtrees`

**Purpose:** Tests backward propagation of an aggressively shared DAG to an AST, producing many duplicate subtrees.

**Precondition:** None.

**Edit (target):** `helperDag.createMulitpleSubtrees()` — builds the 9-node highly shared DAG directly.

**Expected postconditions (`42ByMultipleSubteesAst`, `42ByMultipleSubtreesDag`):** Identical to `BatchForward.testCreateMultipleSameSubtrees`.

---

### `IncrementalForward` — Alignment-based incremental forward propagation (AST → DAG)

These tests establish an initial consistent state, mark all elements with `incrementalID` to enable alignment, apply an incremental edit to the **AST**, and verify that the **DAG** is updated correctly.

The `incrementalID` attribute acts as a stable identity for the incremental synchroniser: elements from a previously-known state that carry the same `incrementalID` are recognised as "the same element" across re-synchronisation.

---

#### `testIncrementalInserts`

**Purpose:** Tests incremental forward propagation when new nodes are inserted into the AST.

**Precondition setup:**
1. `helperAst.createBestDigit()` — creates `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))` in the AST and propagates forward.
2. `helperDag.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all DAG nodes (idle target edit).

**Precondition state:**
- AST (`BestDigitAst`): `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))`
- DAG (`BestDigitIncrIDDag`): flat 4-node DAG sharing `Number(7)`, all nodes have `incrementalID = "incrTestValue"`.

**Edit (source):** `helperAst.insertMoreBestDigits()` — replaces the right `Number(7)` leaf with `Subtract(Multiply(Number(7), Variable("sieben")), Variable("zweiundvierzig"))`:
```
Subtract(
  Multiply(Number(7), Variable("sieben")),   ← existing, incrID preserved
  Subtract(
    Multiply(Number(7), Variable("sieben")), ← new nodes, no incrID
    Variable("zweiundvierzig")               ← new node, no incrID
  )
)
```

**Expected AST postcondition (`MoreBestDigitsAst`):** The full tree above.

**Expected DAG postcondition (`MoreBestDigitsIncrIDDag`):** A 6-node DAG. The original `Multiply(7, sieben)` node is **shared** by both `Subtract` operators (deduplication). The original 4 nodes retain `incrementalID = "incrTestValue"`; the new nodes (second `Subtract` and `Variable("zweiundvierzig")`) have no `incrementalID`.

---

#### `testIncrementalDeletions`

**Purpose:** Tests incremental forward propagation when nodes are deleted from the AST.

**Precondition setup:**
1. `helperAst.createMoreBestDigits()` — creates the full `MoreBestDigits` tree and propagates forward.
2. `helperDag.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all DAG nodes.

**Precondition state:**
- AST (`MoreBestDigitsAst`): the full 9-node `MoreBestDigits` tree.
- DAG (`MoreBestDigitsAllIncrIDDag`): 6-node DAG with all nodes having `incrementalID = "incrTestValue"`.

**Edit (source):** `helperAst.removeSomeBestDigits()` — deletes the right sub-tree (`Subtract(Multiply(Number(7), Variable("sieben")), Variable("zweiundvierzig"))`) and replaces it with a single `Number(7)` leaf.

**Expected AST postcondition (`BestDigitAst`):** `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))` (4 nodes).

**Expected DAG postcondition (`BestDigitIncrIDDag`):** The 4-node DAG sharing `Number(7)`, with `incrementalID = "incrTestValue"` on all nodes. The nodes representing the second `Subtract` and `Variable("zweiundvierzig")` have been deleted.

---

#### `testIncrementalModifications`

**Purpose:** Tests incremental forward propagation when nodes in the AST are modified (attribute changes and structural changes).

**Precondition setup:**
1. `helperAst.createBestDigitRef()` — creates the `BestDigitRef` tree and propagates forward.
2. `helperDag.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all DAG nodes.

**Precondition state:**
- AST (`BestDigitRefAst`):
  ```
  Add(
    Subtract(Multiply(Number(7), Variable("sieben")), Number(7)),
    Multiply(Subtract(Number(14), Number(14)), Divide(Number(7), Number(2)))
  )
  ```
- DAG (`BestDigitRefIncrIDDag`): 10-node DAG; `Number(7)` (value 7) is shared between `Multiply`'s left, `Subtract(7,7)`'s right, and `Divide(7,2)`'s left; `Number(14)` is shared between `Subtract(14,14)`'s left and right; all nodes have `incrementalID = "incrTestValue"`.

**Edit (source):**
- `helperAst.modifyBestDigitRef()` — modifies the left sub-tree:
  - Changes `Subtract(Multiply(7,sieben), 7)` → `Multiply(Subtract(Number(8), Variable("zwei")), Number(7))`
  - Changes `Multiply(Subtract(14,14), Divide(7,2))` → `Multiply(Subtract(14,14), Multiply(Number(14), Number(14)))`
- `helperDag.changeIncrementalIDOf8()` — sets `incrementalID = "incrTestValue8"` on the `Number(8)` node in the DAG.

**Expected AST postcondition (`BestDigitRefModifiedAst`):**
```
Add(
  Multiply(Subtract(Number(8), Variable("zwei")), Number(7)),
  Multiply(Subtract(Number(14), Number(14)), Multiply(Number(14), Number(14)))
)
```

**Expected DAG postcondition (`BestDigitRefModifiedIncrIDDag`):** A 10-node DAG; `Number(14)` is shared across multiple operator positions; the original `Number(7)` and `Variable("sieben")` nodes are replaced with `Number(8)` (having `incrementalID = "incrTestValue8"`) and `Variable("zwei")`; nodes previously aligned via `incrementalID` retain their identity where possible.

---

#### `testIncrementalModificationsResultingInDeletions`

**Purpose:** Tests a specific scenario where a modification in the AST results in previously distinct DAG nodes being merged (deleted) due to structural deduplication.

**Precondition setup:**
1. `helperAst.createSimpleASTRef()` — creates and propagates:
   ```
   Multiply(
     Add(Variable("a"), Number(5)),
     Add(Variable("a"), Number(7))
   )
   ```
   The two `Variable("a")` occurrences map to a **single** shared DAG node.

**Precondition state:**
- AST (`SimpleASTRef`): 7 nodes — `Multiply`, two `Add` operators, two `Variable("a")` leaves, `Number(5)`, `Number(7)`.
- DAG (`SimpleDAGRef`): 6 nodes — `Multiply`, two `Add` operators (distinct due to different right children), one shared `Variable("a")`, `Number(5)`, `Number(7)`.

**Edit:**
1. `helperDag.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all DAG nodes (idle target edit).
2. `helperAst.modifySimpleASTRef()` — changes the `Number(7)` in the right `Add` sub-tree to `Number(5)`, making both `Add` sub-trees structurally identical: both become `Add(Variable("a"), Number(5))`.

**Expected AST postcondition (`SimpleASTRefAfter`):**
```
Multiply(
  Add(Variable("a"), Number(5)),
  Add(Variable("a"), Number(5))
)
```

**Expected DAG postcondition (`SimpleDAGRefAfter`):** Only **4 nodes** (vs. 6 before). The two formerly distinct `Add` operators now deduplicate into one; `Number(5)` is now the single shared right child; `Number(7)` and the second `Add` are **deleted** from the DAG. All 4 nodes carry `incrementalID = "incrTestValue"`.
```
Model
  exprs[0]: Operator(Multiply, incrementalID="incrTestValue", left→exprs[1], right→exprs[1])
  exprs[1]: Operator(Add, leftInverse←exprs[0], rightInverse←exprs[0], incrementalID="incrTestValue", left→exprs[2], right→exprs[3])
  exprs[2]: Variable(name="a", leftInverse←exprs[1], incrementalID="incrTestValue")
  exprs[3]: Number(value=5, rightInverse←exprs[1], incrementalID="incrTestValue")
```

---

#### `testStability` (IncrementalForward)

**Purpose:** Tests that re-running the transformation after an **idle source edit** does not change the target model (stability / round-trip consistency).

**Steps:**
1. `helperAst.createBestDigit()` + propagate forward.
2. `helperDag.changeIncrementalID()` (idle target edit).
3. Assert postcondition `BestDigitAst` / `BestDigitIncrIDDag`.
4. `helperAst.idleDelta()` (empty source edit, re-run propagation).
5. Assert the same postcondition `BestDigitAst` / `BestDigitIncrIDDag` — no change.

---

### `IncrementalBackward` — Alignment-based incremental backward propagation (DAG → AST)

Symmetric to `IncrementalForward`, but the edit direction is reversed: edits are applied to the **DAG** and propagated back to the **AST**.

---

#### `testIncrementalInserts`

**Purpose:** Tests incremental backward propagation when new nodes are inserted into the DAG.

**Precondition setup:**
1. `helperDag.createBestDigit()` — creates and propagates `BestDigit` from DAG.
2. `helperAst.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all AST nodes (idle source edit).

**Precondition state:**
- AST (`BestDigitIncrIDAst`): `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))` — all 5 nodes have `incrementalID = "incrTestValue"`.
- DAG (`BestDigitDag`): the 4-node DAG sharing `Number(7)` (no `incrementalID`).

**Edit (target):** `helperDag.insertMoreBestDigits()` — saves the existing `Multiply(7, sieben)` reference, then replaces the root's right `Number(7)` reference with a new `Subtract(Multiply(7, sieben) [shared], Variable("zweiundvierzig"))` node.

**Expected AST postcondition (`MoreBestDigitsIncrIDAst`):** The `MoreBestDigits` tree; the nodes originally aligned by `incrementalID` retain `incrTestValue`, while newly created AST nodes have no `incrementalID`.

**Expected DAG postcondition (`MoreBestDigitsDag`):** The 6-node shared DAG (no `incrementalID` on any node).

---

#### `testIncrementalDeletions`

**Purpose:** Tests incremental backward propagation when nodes are deleted from the DAG.

**Precondition setup:**
1. `helperDag.createMoreBestDigits()` — creates and propagates `MoreBestDigits` from DAG.
2. `helperAst.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all AST nodes.

**Precondition state:**
- AST (`MoreBestDigitsAllIncrIDAst`): the full 9-node `MoreBestDigits` tree with all nodes having `incrementalID = "incrTestValue"`.
- DAG (`MoreBestDigitsDag`): the 6-node DAG (no `incrementalID`).

**Edit (target):** `helperDag.removeSomeBestDigits()` — saves the `Number(7)` reference, deletes the root's right sub-DAG (`Subtract(Multiply, Variable("zweiundvierzig"))`), and restores `Number(7)` as the root's direct right child.

**Expected AST postcondition (`BestDigitIncrIDDelAst`):**
```
Subtract(                       incrementalID="incrTestValue"
  Multiply(                     incrementalID="incrTestValue"
    Number(7),                  incrementalID="incrTestValue"
    Variable("sieben")          incrementalID="incrTestValue"
  ),
  Number(7)                     (no incrementalID — new node replacing the deleted sub-tree)
)
```

**Expected DAG postcondition (`BestDigitDag`):** The 4-node DAG sharing `Number(7)` (no `incrementalID`).

---

#### `testIncrementalModifications`

**Purpose:** Tests incremental backward propagation when nodes in the DAG are modified.

**Precondition setup:**
1. `helperDag.createBestDigitRef()` — creates and propagates `BestDigitRef` from DAG.
2. `helperAst.changeIncrementalID()` — sets `incrementalID = "incrTestValue"` on all AST nodes.

**Precondition state:**
- AST (`BestDigitRefIncrIDAst`): The full `BestDigitRef` tree with all nodes marked `incrementalID = "incrTestValue"`.
- DAG (`BestDigitRefDag`): The 10-node `BestDigitRef` DAG (no `incrementalID`).

**Edit (target):** `helperDag.modifyBestDigitRef()`:
- On the left subtree: deletes the existing `Number(7)` from `Multiply`'s left and replaces with a new `Number(8)`; renames `Variable("sieben")` → `Variable("zwei")`.
- On the right subtree: saves the reference to `Number(14)`, replaces the `Divide(7,2)` operator with `Multiply`, connects `Number(14)` as both left and right children (referencing the shared `Number(14)` node).

**Expected AST postcondition (`BestDigitRefModifiedIncrIDAst`):**
```
Add(                                              incrementalID="incrTestValue"
  Multiply(                                       incrementalID="incrTestValue"
    Subtract(Number(8), Variable("zwei")),        outer: incrID=incrTestValue, Number(8): no incrID
    Number(7)                                     incrementalID="incrTestValue"
  ),
  Multiply(                                       incrementalID="incrTestValue"
    Subtract(Number(14), Number(14)),             incrID=incrTestValue
    Multiply(Number(14), Number(14))              number 14 nodes: no incrID (new)
  )
)
```

**Expected DAG postcondition (`BestDigitRefModifiedDag`):** The modified 10-node DAG without `incrementalID` attributes.

---

#### `testStability` (IncrementalBackward)

**Purpose:** Tests that re-running the backward transformation after an **idle target edit** does not change the source model.

**Steps:**
1. `helperDag.createBestDigit()` + propagate backward.
2. `helperAst.changeIncrementalID()` (idle source edit).
3. Assert postcondition `BestDigitIncrIDAst` / `BestDigitDag`.
4. `helperDag.idleDelta()` (empty target edit, re-run backward propagation).
5. Assert the same postcondition `BestDigitIncrIDAst` / `BestDigitDag` — no change.

---

## Summary of Tests

| Test Class | Test Method | Direction | Type | Feature Tags |
|---|---|---|---|---|
| `BatchForward` | `testInitialiseSynchronisation` | fwd | batch | fixed |
| `BatchForward` | `testCreateSingleExpression` | fwd | batch | fixed |
| `BatchForward` | `testCreateMultipleExpressions` | fwd | batch | fixed |
| `BatchForward` | `testCreateMultipleExpressionsComplex` | fwd | batch | fixed, sharing |
| `BatchForward` | `testCreateMultipleSameSubtrees` | fwd | batch | fixed, sharing |
| `BatchBackward` | `testCreateSingleExpression` | bwd | batch | fixed |
| `BatchBackward` | `testCreateMultipleExpressions` | bwd | batch | fixed |
| `BatchBackward` | `testCreateMultipleExpressionsComplex` | bwd | batch | fixed, sharing |
| `BatchBackward` | `testCreateMultipleSameSubtrees` | bwd | batch | fixed, sharing |
| `IncrementalForward` | `testIncrementalInserts` | fwd | incremental | add, fixed |
| `IncrementalForward` | `testIncrementalDeletions` | fwd | incremental | del, corr-based, structural |
| `IncrementalForward` | `testIncrementalModifications` | fwd | incremental | attribute, fixed, structural, corr-based |
| `IncrementalForward` | `testIncrementalModificationsResultingInDeletions` | fwd | incremental | attribute, fixed, structural, corr-based, sharing |
| `IncrementalForward` | `testStability` | fwd | incremental | fixed |
| `IncrementalBackward` | `testIncrementalInserts` | bwd | incremental | add, fixed |
| `IncrementalBackward` | `testIncrementalDeletions` | bwd | incremental | del, corr-based, structural |
| `IncrementalBackward` | `testIncrementalModifications` | bwd | incremental | attribute, fixed, structural, corr-based |
| `IncrementalBackward` | `testStability` | bwd | incremental | fixed |

Each test is executed for every active tool implementation (`BXLang`, `BXAgent`), yielding **18 tests × 2 tools = 36 test cases** in total.

---

## Expected Model States (Resource Files)

The `resources/` directory contains XMI files that encode the expected AST and DAG states used by `util.assertPrecondition` and `util.assertPostcondition`:

| Filename | Description |
|---|---|
| `RootElementAst.xmi` | Empty AST root (no expr) |
| `RootElementDag.xmi` | Empty DAG root (no exprs) |
| `42Ast.xmi` | AST: `Number(42)` |
| `42Dag.xmi` | DAG: `Number(42)` |
| `HG2GAst.xmi` | AST: Hitchhiker's Guide sum tree (3 variables, 2 add operators) |
| `HG2GDag.xmi` | DAG: same sum, flat 5-node structure |
| `42ByMultiplyAddSubtractDivideAst.xmi` | AST: complex arithmetic tree with repeated sub-expressions |
| `42ByMultiplyAddSubtractDivideDag.xmi` | DAG: deduplicated version with 9 nodes |
| `42ByMultipleSubteesAst.xmi` | AST: large tree of `Number(1)` leaves |
| `42ByMultipleSubtreesDag.xmi` | DAG: aggressively deduplicated, only 9 nodes |
| `BestDigitAst.xmi` | AST: `Subtract(Multiply(Number(7), Variable("sieben")), Number(7))` |
| `BestDigitDag.xmi` | DAG: 4-node version sharing `Number(7)` (no `incrementalID`) |
| `BestDigitIncrIDAst.xmi` | AST: `BestDigit` with `incrementalID="incrTestValue"` on all nodes |
| `BestDigitIncrIDDag.xmi` | DAG: `BestDigit` with `incrementalID="incrTestValue"` on all nodes |
| `BestDigitIncrIDDelAst.xmi` | AST: `BestDigit` after backward deletion; original nodes keep `incrID`, new right `Number(7)` does not |
| `MoreBestDigitsAst.xmi` | AST: `MoreBestDigits` tree (9 nodes) |
| `MoreBestDigitsDag.xmi` | DAG: `MoreBestDigits`, 6 nodes, `Multiply(7,sieben)` shared |
| `MoreBestDigitsIncrIDAst.xmi` | AST: `MoreBestDigits` with `incrID` on original (previously existing) nodes only |
| `MoreBestDigitsIncrIDDag.xmi` | DAG: `MoreBestDigits` with `incrID` on originally aligned nodes only |
| `MoreBestDigitsAllIncrIDAst.xmi` | AST: `MoreBestDigits` with `incrID` on all nodes |
| `MoreBestDigitsAllIncrIDDag.xmi` | DAG: `MoreBestDigits` with `incrID` on all nodes |
| `BestDigitRefAst.xmi` | AST: `BestDigitRef` — 11-node tree with two occurrences of `Number(7)` |
| `BestDigitRefDag.xmi` | DAG: `BestDigitRef` — 10-node DAG with shared `Number(7)` and `Number(14)` |
| `BestDigitRefIncrIDAst.xmi` | AST: `BestDigitRef` with `incrID` on all nodes |
| `BestDigitRefIncrIDDag.xmi` | DAG: `BestDigitRef` with `incrID` on all nodes |
| `BestDigitRefModifiedAst.xmi` | AST: modified `BestDigitRef` — `sieben→zwei`, `7→8` on left; `Divide→Multiply(14,14)` on right |
| `BestDigitRefModifiedDag.xmi` | DAG: corresponding 10-node modified DAG |
| `BestDigitRefModifiedIncrIDAst.xmi` | AST: modified `BestDigitRef` with partial `incrID` retention |
| `BestDigitRefModifiedIncrIDDag.xmi` | DAG: modified `BestDigitRef` with partial `incrID` retention |
| `SimpleASTRef.xmi` | AST: `Multiply(Add(a,5), Add(a,7))` — two distinct `Add` sub-trees |
| `SimpleDAGRef.xmi` | DAG: 6-node version with shared `Variable("a")` |
| `SimpleASTRefAfter.xmi` | AST: `Multiply(Add(a,5), Add(a,5))` — right `7` changed to `5` |
| `SimpleDAGRefAfter.xmi` | DAG: 4-node version — the two `Add` operators deduplicate into one shared node; all have `incrID` |
