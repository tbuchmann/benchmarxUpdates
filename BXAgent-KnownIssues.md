# Known BXAgent Issues

Bugs and gaps discovered in the `BXAgent` tool implementations while filling
round-trip and concurrent test coverage across the benchmarx examples (see the
per-example `concurrent/`/`alignment_based/roundtrip/` test suites). Bugs 1, 2, and 3
all need a fix in the `bxagent`/`emt-agent` generator repos (sibling repos that generate
the `BXAgent*` transformation code), followed by a jar rebuild/copy back into this repo
and re-verification of the affected test(s). None of them are fixable from
`benchmarxUpdates` directly — the adapter classes here just call into generated code.

Found and documented: 2026-07-17 (bugs 1, 2); 2026-07-22 (bug 3, after the original
stub was wired up and found to expose a deeper generator bug); 2026-07-23 (bug 3
narrowed after a `bxagent`-repo fix landed — updates now work, creation/deletion still
don't).

## Summary

| # | Title | Examples affected | Trigger | Data-loss severity |
|---|---|---|---|---|
| 1 | `Operator.op` lost during conflict resolution | Ast2Dag | Concurrent **conflict** (both sides edit the same shared/structurally-relevant node) | High — AST/DAG end up structurally inconsistent |
| 2 | Target-side edits dropped during concurrent sync | Gantt2Cpm, Set2OSet | Concurrent edit where target touches an attribute that has a **real source-side correspondence counterpart** | High — source/target end up permanently divergent, not just "resolved differently" |
| 3 | `sync()` never creates/deletes role-based-type (EClass/Column-level) objects | Ecore2SQL | Any concurrent edit that creates or deletes an EClass/EAttribute — renames/value-changes to *already-corresponded* objects now work as of 2026-07-23 | Moderate — blocks `MonotonicCreating`/`MonotonicDeleting`/`NonMonotonic`; `Conflicts` (rename-vs-rename) now passes |

**Working, not affected**: concurrent target-side edits to attributes with **no
source-side counterpart** (`weightA1BWith73` in pntopnw — weight only exists on the
weighted/target side; `changeIncrementalID` in asttodag — pure local metadata) complete
correctly every time. This is a useful diagnostic: bug 2 only triggers when the
target's concurrently-changed attribute is meant to be mirrored back to source at all.
If an attribute is legitimately target-only, there's nothing to drop, so those tests
stay green. See the `NonMonotonic`/`MonotonicCreating` tests in `pntopnw` and
`asttodag` for confirmed-clean examples of this category.

**Possible relationship between bug 1 and bug 2**: both manifest as *attribute data
loss specifically around conflict/concurrent-resolution code paths* that don't occur on
the plain forward/backward-only paths (which are exercised by nearly every other
passing test in this suite and are demonstrably correct). It's plausible both trace
back to the same underlying gap — e.g. a shared "recompute target attributes after
conflict/concurrent merge" step in `bx-runtime` that isn't invoked, or isn't invoked
completely, outside the simple one-directional propagation path. Worth investigating
together rather than assuming two unrelated bugs; if a `bx-runtime` fix for bug 2 also
resolves bug 1, that would confirm the shared root cause.

---

## 1. Ast2Dag: `Operator.op` attribute lost during conflict resolution

**Status**: confirmed, reproducible, not fixed.

**Where**: `examples/asttodag/BenchmarxAstToDag`, tool `BXAgentAst2Dag`. Reproduced by
`Conflicts.testConcurrentRenameSharedVariableConflict` in
`examples/asttodag/BenchmarxAstToDag/src/org/benchmarx/examples/ast2dag/testsuite/concurrent/Conflicts.java`
(currently `@Disabled`, referencing this file).

**Reproduction**:
1. Build the `BestDigitRef` precondition via forward source edit: `create42`,
   `createBestDigit`, `createBestDigitRef` (via `performAndPropagateSourceEdit`).
2. Concurrent step via `performAndPropagateEdit`:
   - SRC: `AstHelper.modifyBestDigitRef()` (existing helper — renames the shared
     `"sieben"` variable to `"zwei"`, changes several numeric values, converts a
     `Divide` operator subtree into `Multiply`).
   - TRG: `DagHelper.renameSharedVariableSieben()` (added for this test — a direct,
     non-navigation-based EMF mutation that renames the same shared `"sieben"` DAG
     variable to `"unbekannt"`).

**The bug**: the naming conflict itself resolves correctly (source wins: variable ends
up `"zwei"` on both sides). But in the same resolution, **two `Operator` nodes on the
DAG side lose their `op` attribute** — it silently reverts to the EMF default
(`ArithmeticOperator.Add`, confirmed as the declared-first/default literal in both
`ExpressionAST.ecore` and `ExpressionDAG.ecore`) instead of retaining the correct
`Multiply`. The corresponding AST-side nodes correctly retain `op="Multiply"`.

Net effect: **the AST and DAG models end up structurally inconsistent with each
other** after this specific concurrent-conflict scenario — an actual attribute-loss
bug in BXAgent's conflict-resolution/backward-sync path, not just "resolved
differently than expected."

**Hypothesis (unconfirmed)**: likely related to how BXAgent recomputes/re-fingerprints
`Operator` nodes when a conflict-resolution pass has to re-derive or re-match nodes on
the target (DAG) side that were touched independently on both sides — the `op`
attribute may not be carried through whatever update path runs during conflict merge
(as opposed to the normal forward/backward propagation path, where `op` is set
correctly, per every other passing test in this suite). Worth checking whether this is
specific to `Ast2Dag`'s generated `computeFingerprintBack`/attribute-update logic, or a
more general BXAgent conflict-resolution code path shared across examples.

**To resume**: reproduce via the disabled test, dig into the BXAgent-generated
`Ast2DagTransformation` conflict-resolution/merge logic in the `bxagent` repo, fix,
rebuild jars, copy into `benchmarxUpdates/examples/asttodag/*/lib`, then re-enable the
test (fixtures `ConflictsSharedVarSrcWinsAst.xmi`/`ConflictsSharedVarSrcWinsDag.xmi` are
not yet created since the test is disabled — capture fresh via `tool.saveModels(...)`
once the fix is in place).

**Concrete starting points**: in the generated `Ast2DagTransformation.sync(...)` (the
method `performAndPropagateEdit` ultimately calls, per the `BXTool` architecture — see
this repo's root `CLAUDE.md` for the `performAndPropagateEdit` → `sync` call chain),
find the branch that handles a detected conflict on a node touched by both sides.
Compare what it does to `Operator` nodes against what the plain forward-transform path
(`transform`/`transformIncremental`) does — the forward path is known-correct (every
non-conflict test passes), so a side-by-side diff of "what attributes get (re)written"
between the two code paths should surface where `op` is dropped. Also check
`dev.bxagent.correspondence.*` (in `bx-runtime`) for any generic
post-conflict-resolution model cleanup/rewrite step that might not be attribute-type-aware.

---

## 2. Target-side edits dropped during concurrent sync (seen in Gantt2Cpm AND Set2OSet)

**Status**: confirmed, reproducible, not fixed. Seen in two unrelated examples with two
different generated transformations, which points at a shared BXAgent runtime bug
(likely in `bx-runtime`/`dev.bxagent.correspondence.*`, not per-example generated code)
rather than something specific to one transformation.

### 2a. Gantt2Cpm

**Where**: `examples/gantttocpm/BenchmarxGanttToCPM`, tool `BXAgentGantt2Cpm`.

**Reproduction**:
- **Works (non-concurrent)**: `IncrementalBackward.testIncrementalValueChange` calls
  `CPMHelper.changeCPM2GanttModelDuration()` (sets `GanttModel` duration to 0,
  `CPMModel` duration to 4 on the CPM/target side) via
  `tool.performAndPropagateTargetEdit(...)`. The resulting fixture
  `TestsBuilderMModel-ComparatorGantt.xmi` confirms this correctly backward-propagates
  to the Gantt/source side.
- **Broken (concurrent)**: calling the exact same
  `CPMHelper.changeCPM2GanttModelDuration()` inside a concurrent step —
  `tool.performAndPropagateEdit(srcEdit(helperGantt::deleteGantt2CPMHelpers),
  trgEdit(helperCPM::changeCPM2GanttModelDuration))`, where the two edits touch
  entirely disjoint activities (Helpers vs. Models, no overlap) — produces a target
  (CPM) model where the change applied locally as expected, but the **source (Gantt)
  side never receives the backward-propagated change**: `GanttModel`/`CPMModel`
  durations stay at their original values instead of updating. Captured via
  `tool.saveModels(...)`.

Since every *pre-existing* concurrent test in this example
(`MonotonicCreating`/`MonotonicDeleting`) keeps the target side purely idle
(`trgEdit(helperCPM::idleDelta)`), this is the first time the concurrent path was
exercised with a real target-side edit — so the bug was previously invisible.

**Workaround applied**: new `NonMonotonic`/`Conflicts` tests added to `gantttocpm` keep
the target side idle (or, for `Conflicts`, only assert the actually-observed outcome)
to avoid this gap rather than working around it test-by-test.

### 2b. Set2OSet

**Where**: `examples/settooset/BenchmarxSetToOSet`, tool `BXAgentSet2OSet`.

**Reproduction**:
- **Works (non-concurrent)**: `IncrementalBackward.testIncrementalValueChange` calls
  `OsetHelper.changeABCtoZXY()` (renames A/B/C to Z/X/Y on the Oset/target side) via
  `tool.performAndPropagateTargetEdit(...)`. The resulting fixture
  `ZxyChangedSet.xmi` confirms this correctly backward-propagates to the Set/source
  side.
- **Broken (concurrent)**: in a `Conflicts` test scenario —
  `tool.performAndPropagateEdit(srcEdit(helperSet::deleteA),
  trgEdit(helperOset::changeABCtoZXY))` — source deletes A (which conflicts with
  target's rename of A) while target *also* independently renames the uncontested B
  and C to X and Y. The delete-vs-rename conflict on A resolved fine (delete won,
  consistently on both sides), but **B/C's rename never backward-propagated**: target
  ended up `{X, Y}` while source stayed at `{B, C}` — a genuinely inconsistent,
  divergent result between the two models, not just "a valid resolution policy".
  Captured via `tool.saveModels(...)`.

**Workaround applied**: added a narrower `OsetHelper.renameAToZ()` (touches only the
contested element, not the uncontested collateral data `changeABCtoZXY` also touches)
so the `Conflicts` test in this example doesn't depend on backward-propagating
non-conflicting target edits. With the conflict isolated to a single element, the
result came back clean and consistent on both sides.

### The bug (general)

`performAndPropagateEdit`'s concurrent reconciliation applies the source-side edit
correctly (forward propagation works in both examples) but silently drops
non-conflicting target-side edits instead of backward-propagating them to source, even
though the identical operation backward-propagates correctly via the dedicated
non-concurrent `performAndPropagateTargetEdit` path. Confirmed in two independently
generated transformations, so likely a shared BXAgent runtime issue rather than a
per-example code-generation bug — worth checking `bx-runtime`'s concurrent-sync
implementation first before digging into either example's generated transformation
code.

**General workaround** (applied across examples going forward): keep target-side edits
either idle, or scoped to *only* the contested element in `Conflicts` tests, until this
is fixed upstream. Any future concurrent test that requires the target to make a real,
independently-propagating edit alongside unrelated source changes should be expected to
hit this same gap.

**To resume**: reproduce via the scenarios above (both 2a and 2b — having two
independent repros in different generated transformations is valuable for confirming a
shared-code fix actually resolves both), dig into the BXAgent-generated
`Gantt2CpmTransformation`'s and `Set2OSetTransformation`'s concurrent/
`performAndPropagateEdit` code path in the `bxagent` repo, compare against the working
`performAndPropagateTargetEdit`-only path, fix, rebuild jars, copy into
`benchmarxUpdates`, then redesign the affected tests to exercise genuine bidirectional
concurrent edits (drop the "keep target idle"/"scope to one element" workarounds once
fixed, to get back to testing the real bidirectional-concurrent behavior these tests
were originally meant to exercise).

**Concrete starting points**: since this reproduces identically in two separately
generated transformations, look first at the **shared** `bx-runtime` code
(`dev.bxagent.correspondence.*` — `CorrespondenceModel`, `TransformationContext`, or
whatever orchestrates a concurrent `sync()` call across both generated transformations)
rather than either example's generated code. Specifically: does the concurrent sync
path call the same "update target attribute → mirror to source" routine that the
dedicated `performAndPropagateTargetEdit` path calls, or a different/partial one? The
non-concurrent path is proven correct (`IncrementalBackward.testIncrementalValueChange`
passes in both examples), so it's a good reference implementation to diff against.

---

## 3. Ecore2SQL: `Ecore2SqlTransformation.sync()` doesn't create/delete role-based-type objects

**Status**: stub wired up 2026-07-22 (owner: user); empirical testing that day found
the underlying generated `sync()` method non-functional for essentially every
scenario touching an `EClass`. On 2026-07-23 the user landed a fix in the `bxagent`
repo and regenerated `Ecore2SqlTransformation.java`; the jars in `~/.m2` were rebuilt
from it. Re-testing confirmed the fix covers **updates** to already-corresponded
role-based objects (renames, attribute value changes — including two-sided conflicts)
but **not** creation or deletion of new role-based objects during a concurrent
`sync()` call, which is a narrower, still-open gap. This is still a **`bxagent`-repo
fix** for the remaining piece, not something fixable in `benchmarxUpdates`.

**Where**: `examples/ecoretosql/BenchmarxEcoreToSQL/src/org/benchmarx/examples/ecore2sql/implementations/bxagent/BXAgentEcore2SQL.java`,
method `performAndPropagateEdit`, now:

```java
public void performAndPropagateEdit(Supplier<IEdit<EPackage>> sourceEdit, Supplier<IEdit<Schema>> targetEdit) {
    sourceEdit.get();
    targetEdit.get();
    Ecore2SqlTransformation.sync(source, target, corr,
            SyncConflictPolicy.TARGET_WINS,
            TransformationContext.DeletionPolicy.CASCADE, Ecore2SqlTransformation.Options.defaults());
}
```

This correctly calls into `de.tbuchmann.bxagent.ecore2sql.Ecore2SqlTransformation.sync(...)`
(from the `bxagent` sibling repo, `generated/ecore2sql/Ecore2SqlTransformation.java`),
using the same `dev.emtagent.correspondence.SyncConflictPolicy`/`TransformationContext`
runtime this example already depends on for its working `transform`/`transformBack`
calls. The wiring itself is correct — this is *not* a repeat of the original stub bug.

**Note on file location**: the correct generated source is the **top-level**
`bxagent/generated/Ecore2SqlTransformation.java` (package
`de.tbuchmann.bxagent.ecore2sql`, matching `BXAgentEcore2SQL.java`'s import). There is
a *different*, stale file at `bxagent/generated/ecore2sql/Ecore2SqlTransformation.java`
(package `dev.emtagent.generated`) that looks superficially identical but belongs to a
different generation path — the original 2026-07-22 root-cause analysis below was
written against that wrong file. Its diagnosis (the missing-method comment reference)
turned out to still be accurate for the correct file too, but double-check the package
declaration before reading generated ecoretosql source in future sessions.

**What's now fixed (2026-07-23)**: `sync()`'s change-detection loop (`Schritt 1`,
iterating existing correspondence entries) used to special-case "TypeMapping types"
(objects where `isCoveredByTypeMappingSource`/`Target` returns true — in this
transformation, only `EPackage`/`Schema` are TypeMapping types) versus "role-based
types" (everything else — `EClass`/`Table`, `EAttribute`/`Column`, etc.). Only
TypeMapping types got real Fall A/B (one-sided update) handling; role-based types fell
into a branch that `continue`d without propagating, referencing a
`mapRoleBasedTypesIncremental`/`Back` method that was never actually defined. The fix
removes that distinction for **updates**: role-based types now go through the same
inline `updateTargetAttributes`/`updateSourceAttributes` Fall A/B/C logic as
TypeMapping types. Verified via `examples/ecoretosql/.../testsuite/concurrent/Conflicts.java`
(`testConcurrentRenameListLengthConflict` — a rename-vs-rename conflict on
`List.length`/the `length` column, resolved deterministically via the adapter's
hardcoded `TARGET_WINS`) and via an ad hoc single-sided rename probe
(`EcoreHelper.renameListClass` with target idle) — both now propagate correctly where
they previously produced an all-zero `SyncResult`.

**What's still broken**: **creation and deletion** of role-based objects during
`sync()`. The partitions that handle new/removed objects (`Schritt 2` "new target
object from unmatched source", `Schritt 3` "source-deleted, target kept", `Schritt 5`
"new objects without corrEntry") all still gate on `isCoveredByTypeMappingSource`/
`Target` — i.e. only `EPackage`/`Schema` — so a brand-new `EClass`/`EAttribute` created
concurrently, or an existing one deleted concurrently, is silently ignored by `sync()`
just like updates used to be. Confirmed via two probes:
- `EcoreHelper.addDataElementFeature()` (adds four new EClasses: `DataElement`, `Pair`,
  `Value`, `Key`) concurrent with target idle — source model correctly shows the new
  classes afterward (`sourceEdit.get()` always mutates locally), but **no
  corresponding tables appear in the target SQL schema**.
- `EcoreHelper.deleteDataAttribute()` (deletes `DataNode.data`) concurrent with target
  idle — source model correctly loses the attribute, but **the `data` column still
  exists in the target SQL schema** afterward.

**Practical impact**: `MonotonicCreating`, `MonotonicDeleting`, and `NonMonotonic` (per
the standing project convention, the latter mixes creation and/or deletion — see this
repo's `CLAUDE.md`) all fundamentally require the still-broken creation/deletion path
and remain blocked. `Conflicts` no longer needs to — a same-element rename-vs-rename
conflict only exercises the now-fixed update path — so it's written and passing
(`testConcurrentRenameListLengthConflict`, 22/22 tests green for `BXAgentEcore2SQL` as
of 2026-07-23). `RoundtripTests` remains unaffected either way (it only uses
`performAndPropagateSourceEdit`/`performAndPropagateTargetEdit`, which go through
`transform`/`transformBack` instead of `sync`).

**Likely fix** (in the `bxagent` repo): extend `Schritt 2`/`3`/`5`'s
`isCoveredByTypeMappingSource`/`Target` guards to also cover role-based types, reusing
`createNewTargetObject`/`createNewSourceObject` (which already dispatch on `EPackage`
only — would need `EClass`/`EAttribute` cases added) and the existing
`EcoreUtil.delete`-based deletion logic from the TypeMapping partition-3 branch. Worth
checking whether `bxagent`'s `fixedCode/correct/Ecore2SqlTransformation.java` already
covers this before re-deriving it.

**Precondition fixtures already captured for when this is fixed**:
`MonotonicCreatingPreEcore.ecore`/`MonotonicCreatingPreSQL.xmi` and
`MonotonicDeletingPreEcore.ecore`/`MonotonicDeletingPreSQL.xmi` in
`examples/ecoretosql/BenchmarxEcoreToSQL/resources/` (simple CompositeList state and the
richer CompositeListData state respectively — reusable as starting points once
creation/deletion in `sync()` is fixed).
