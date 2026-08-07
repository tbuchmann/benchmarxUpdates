# Known BXAgent Issues

Bugs and gaps discovered in the `BXAgent` tool implementations while filling
round-trip and concurrent test coverage across the benchmarx examples (see the
per-example `concurrent/`/`alignment_based/roundtrip/` test suites). Bug 1 still needs a
fix in the `bxagent`/`emt-agent` generator repos (sibling repos that generate the
`BXAgent*` transformation code), followed by a jar rebuild/copy back into this repo and
re-verification of the affected test — not fixable from `benchmarxUpdates` directly,
since the transformation logic itself lives in generated code. Bugs 2 and 3 have both
been fixed this way (fix landed in `bxagent`, jars rebuilt) and are now **resolved**.

Found and documented: 2026-07-17 (bugs 1, 2); 2026-07-22 (bug 3, after the original
stub was wired up and found to expose a deeper generator bug); 2026-07-23 (bug 3
narrowed after a first `bxagent`-repo fix landed — updates worked, creation/deletion
still didn't; corrected same day from an initial wrong "creation propagates nothing"
claim to the precise "creation produces structurally empty stubs, deletion leaves
orphaned/corrupted references"; a second `bxagent`-repo fix later the same day resolved
both remaining halves — all three previously-`@Disabled` reproduction tests in
`Conflicts.java` now pass unchanged, confirming the fix against the exact assertions
that caught the bug); 2026-08-05 (bug 2's vague "likely a shared `bx-runtime` bug"
diagnosis replaced with two concrete, code-verified root causes — see
`BXAgent-KnownIssues-Fixes.md` — and two new `@Disabled` reproduction tests added,
`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` in gantttocpm
and `Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict` in settooset, both using
genuine unrestricted target-side edits instead of the idle-target/scoped-conflict
workarounds the existing passing tests use); **2026-08-07 (bug 2 RESOLVED)** — Fix 1
(switching `performAndPropagateEdit` from `transform()` to `sync()`) applied to all four
affected adapters (`BXAgentGantt2Cpm`, `BXAgentSet2OSet`, and two more found to share the
exact same defect while auditing the rest, `BXAgentAst2Dag` and `BXAgentPn2Pnw`), then
Fix 2 (the `sync()` deletion-cascade generator gap) landed in `bxagent` and fixed the
resulting regressions in all four; a related but distinct creation-propagation gap
surfaced and was fixed in `ast2dag`'s generated `sync()` specifically. Both `@Disabled`
reproduction tests from 2026-08-05 now pass unchanged and have been re-enabled; full
BXAgent suite across all 8 examples is 239/239 passing, 0 failures.

## Summary

| # | Title | Examples affected | Trigger | Data-loss severity |
|---|---|---|---|---|
| 1 | `Operator.op` lost during conflict resolution | Ast2Dag | Concurrent **conflict** (both sides edit the same shared/structurally-relevant node) | High — AST/DAG end up structurally inconsistent |
| 2 | ~~Target-side edits dropped during concurrent sync~~ **RESOLVED 2026-08-07** | Was: Gantt2Cpm, Set2OSet (and the same adapter-wiring defect independently found in Ast2Dag, Pn2Pnw while auditing) | Was: concurrent edit where target touches an attribute that has a **real source-side correspondence counterpart** | Fixed upstream in `bxagent`; all 4 previously-affected examples plus the two `@Disabled` reproduction tests pass |
| 3 | ~~`sync()` creates empty Table stubs / leaves orphaned columns on delete~~ **RESOLVED 2026-07-23** | Ecore2SQL | Was: any concurrent edit that creates or deletes an EClass/EAttribute | Fixed upstream in `bxagent`; all 4 `Conflicts` tests (including 3 former reproduction cases) pass |

**Working, not affected**: concurrent target-side edits to attributes with **no
source-side counterpart** (`weightA1BWith73` in pntopnw — weight only exists on the
weighted/target side; `changeIncrementalID` in asttodag — pure local metadata) complete
correctly every time. This is a useful diagnostic: bug 2 only triggers when the
target's concurrently-changed attribute is meant to be mirrored back to source at all.
If an attribute is legitimately target-only, there's nothing to drop, so those tests
stay green. See the `NonMonotonic`/`MonotonicCreating` tests in `pntopnw` and
`asttodag` for confirmed-clean examples of this category.

**Relationship between bug 1 and bug 2 — superseded.** This section originally
speculated both bugs shared a root cause in `bx-runtime`. Bug 2's actual root causes
(found 2026-08-05, see `BXAgent-KnownIssues-Fixes.md`) are unrelated to bug 1: the
`BXAgentGantt2Cpm`/`BXAgentSet2OSet` adapters simply never call `sync()` at all (they
call the forward-only `transform()`), and separately, `sync()`'s generated code has no
deletion-cascade handling. Neither of those is a "recompute target attributes after
conflict" gap, and neither is shared `bx-runtime` code — both are specific to each
example's generated `Gantt2CpmTransformation`/`Sets2OsetsTransformation` classes and
their adapters. Bug 1 (`Operator.op` loss in Ast2Dag) remains open and unexplained by
this finding; there is no longer a basis for assuming it shares a cause with bug 2.

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

**Status: RESOLVED 2026-08-07.** Fixed by applying both fixes from
`BXAgent-KnownIssues-Fixes.md`: Fix 1 (switching `performAndPropagateEdit` from
`transform()` to `sync()` in `benchmarxUpdates`) applied to `BXAgentGantt2Cpm` and
`BXAgentSet2OSet`, plus `BXAgentAst2Dag` and `BXAgentPn2Pnw`, found to have the exact
same defect while auditing the rest of the examples; and Fix 2 (the `sync()`
deletion-cascade generator gap) landed in the `bxagent` repo. Verified by re-enabling
both `@Disabled` reproduction tests from 2026-08-05
(`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` in
gantttocpm, `Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict` in settooset)
without changing their assertions — both pass. Full BXAgent suite across all 8 examples:
239/239 passing, 0 failures. The rest of this section is kept as historical diagnosis.

**Original status (superseded)**: confirmed, reproducible, not fixed. Seen in two
unrelated examples with two different generated transformations, which at the time
suggested a shared BXAgent runtime bug (likely in `bx-runtime`/`dev.bxagent.correspondence.*`,
not per-example generated code) rather than something specific to one transformation —
this guess was later corrected, see "The bug (general)" below.

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

**Workaround applied (superseded 2026-08-07)**: new `NonMonotonic`/`Conflicts` tests
added to `gantttocpm` kept the target side idle (or, for `Conflicts`, only asserted the
actually-observed outcome) to avoid this gap rather than working around it test-by-test.
No longer necessary — `BXAgentGantt2Cpm.performAndPropagateEdit` now calls `sync()`
correctly and the underlying generator gap is fixed; the dedicated reproduction test
(`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration`) is
re-enabled and passes.

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

**Workaround applied (superseded 2026-08-07)**: added a narrower `OsetHelper.renameAToZ()`
(touches only the contested element, not the uncontested collateral data
`changeABCtoZXY` also touches) so the `Conflicts` test in this example didn't depend on
backward-propagating non-conflicting target edits. No longer necessary —
`BXAgentSet2OSet.performAndPropagateEdit` now calls `sync()` correctly and the
underlying generator gap is fixed; the dedicated reproduction test
(`Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict`, using the broader
`changeABCtoZXY`) is re-enabled and passes. `renameAToZ()`-based test is left in place
unchanged (harmless, just no longer load-bearing).

### The bug (general) — root cause found 2026-08-05

Originally documented (2026-07-17) only as an observed symptom — non-conflicting
target-side edits silently dropped during concurrent sync — with a guess that it was a
shared `bx-runtime` issue. That guess was wrong. The actual root cause, found by
prototyping fixes directly and using two new `@Disabled` reproduction tests as the
oracle (`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` in
gantttocpm, `Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict` in settooset), is
two separate, smaller defects. Full analysis and proposed fixes in
`BXAgent-KnownIssues-Fixes.md`; summary:

1. **`BXAgentGantt2Cpm.performAndPropagateEdit` and `BXAgentSet2OSet.performAndPropagateEdit`
   never call `sync()` at all** — they call the plain forward-only `transform()`. This
   alone explains the entire symptom: `transform()` only ever reads source → target, so
   target-only changes are never even looked at, and "conflicts" only appeared resolved
   because forward-only `transform()` unconditionally overwrites target from source. For
   comparison, `BXAgentEcore2SQL.performAndPropagateEdit` already calls `sync()`
   correctly (that's why bug 3 above is fully resolved and this bug never affected
   Ecore2SQL).
2. **Fixing (1) alone is not enough**: `sync()`'s generated code in both
   `Gantt2CpmTransformation.java` and `Sets2OsetsTransformation.java` never calls
   `CorrespondenceModel.findDeletedSourceEntries`/`findDeletedTargetEntries` — unlike the
   dedicated `transformIncremental`/`transformIncrementalBack` paths, which do. So
   `sync()` cannot express "this object was deleted on one side, cascade-delete the
   other side" during a concurrent step; it silently reinterprets a one-sided deletion as
   "the other side created something new" and recreates the deleted object. Confirmed by
   prototyping: swapping `transform()` → `sync()` in the adapter (with hooks correctly
   passed, fixing a separate hook-wiring issue found along the way — see
   `BXAgent-KnownIssues-Fixes.md`) made the target-edit backward-propagation case pass,
   but broke three previously-passing deletion tests, all showing the deleted object
   reappearing after `sync()`.

Neither defect is shared `bx-runtime` code, and neither is a "recompute target
attributes after conflict" gap as originally guessed — see the now-corrected
"Relationship between bug 1 and bug 2" note in the Summary section above.

**Resolution (2026-08-07)**: both fixes landed, in the recommended order. Fix 2
(`sync()`'s missing deletion-cascade handling) landed first in the `bxagent` repo and
jars were rebuilt. Fix 1 (`benchmarxUpdates` adapter wiring) was then applied — and
while auditing the other examples for the same defect, `BXAgentAst2Dag` and
`BXAgentPn2Pnw` turned out to have the identical `transform()`-instead-of-`sync()` bug,
so they were fixed the same way. Applying Fix 1 alone (before Fix 2 had actually landed
in the jars this repo builds against) initially surfaced the deletion-cascade gap as 11
concrete test regressions across all four newly-switched examples, plus a separate,
newly-discovered creation-propagation gap specific to `ast2dag`'s generated `sync()`
(source-side creations weren't reaching the target). Once Fix 2's jar was correctly
rebuilt and the `ast2dag` creation-propagation gap was also fixed upstream, all 11
regressions resolved and both `@Disabled` reproduction tests (re-enabled, unchanged
assertions) pass. Full results in `BXAgent-TestSummary.md`.

---

## 3. Ecore2SQL: `Ecore2SqlTransformation.sync()` doesn't create/delete role-based-type objects

**Status: RESOLVED 2026-07-23.** Fixed in two rounds in the `bxagent` repo, both on
2026-07-23. The first round fixed **updates** (see below); a second round the same
day fixed the remaining **creation** (empty Table stubs) and **deletion** (orphaned
columns/foreign keys on `EObject`) gaps. Verified by re-enabling the three
`@Disabled` reproduction tests in
`examples/ecoretosql/BenchmarxEcoreToSQL/src/org/benchmarx/examples/ecore2sql/testsuite/concurrent/Conflicts.java`
(`testMonotonicCreating`, `testMonotonicDeleting`, `testNonMonotonic`) without
touching their assertions — all three now pass, along with the rest of the module
(25/25, 0 skipped). The rest of this section is kept as historical diagnosis (useful
if a regression ever reintroduces this class of bug).

**Original status (superseded)**: stub wired up 2026-07-22 (owner: user); empirical
testing that day found the underlying generated `sync()` method non-functional for
essentially every scenario touching an `EClass`. On 2026-07-23 the user landed a first
fix in the `bxagent` repo and regenerated `Ecore2SqlTransformation.java`; the jars in
`~/.m2` were rebuilt from it. Re-testing confirmed that fix covered **updates** to
already-corresponded role-based objects (renames, attribute value changes — including
two-sided conflicts) but **not** creation or deletion of new role-based objects during
a concurrent `sync()` call — see "What's still broken" below for what that gap
actually looked like. A second fix later the same day closed it; see the **RESOLVED**
note above.

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

**Correction (2026-07-23, same day)**: the paragraph above (and the two probes it
cites) is **wrong** about creation producing no result at all — that was a testing
mistake (grepped a stale dump file after switching probe scenarios, before the file
had been regenerated). The real picture, found while reviewing user-authored
`testMonotonicCreating`/`testMonotonicDeleting`/`testNonMonotonic` tests that
initially (and misleadingly) passed against only a table-count assertion:

**Creation**: `sync()` *does* create a `Table` for every new `EClass`, via
`createAndMapCTMObjectsIncremental` (`bxagent/generated/Ecore2SqlTransformation.java`,
around line 860) — a code path that runs unconditionally near the end of `sync()`,
entirely separate from the `isCoveredByTypeMappingSource`/`Target`-gated partitions
(`Schritt 2`/`3`/`5`) described above, which is why grepping those didn't surface it.
But the `Table` it creates is an empty shell: only `.setName(...)` and a
`class`+(`concrete`|`abstract`) annotation. It has **no `id` column, no primary key,
no foreign keys, and no attribute/reference columns** — everything
`SQLHelper.createXTable()`/the working forward-transform path always produces. A
naive "one class-`Table` per `EClass`" count check passes, because the stub *is* a
`Table` with the right name and annotation — it just isn't usable.

**Deletion**: `sync()` does correctly remove the `Table` objects for deleted
`EClass`es (so a naive count check passes here too), but leaves their "unique"
identity column and foreign key **behind on the `EObject` root table** — the column
that every class-`Table` gets on the shared root table when it's created (see e.g.
`SQLHelper.createDataElementTable()`'s `"Add DataElement column to EObject table"`
step) is never cleaned up. Worse than a mere leak: of the four orphaned foreign keys
observed, three end up with no `referencedTable` at all (dangling), and the fourth
silently ends up pointing at an unrelated surviving table (`List`) — an artifact of
the deletion leaving a stale position-based reference that gets re-resolved against
the now-shrunk `ownedTables` list at serialization time. This is referential
corruption on the *surviving* part of the schema, not just an incomplete propagation
of the deleted part.

**Practical impact (while open)**: `MonotonicCreating`, `MonotonicDeleting`, and
`NonMonotonic` (per the standing project convention, the latter mixes creation and/or
deletion — see this repo's `CLAUDE.md`) were written with assertions that actually
check structure (not just counts) in
`examples/ecoretosql/BenchmarxEcoreToSQL/src/org/benchmarx/examples/ecore2sql/testsuite/concurrent/Conflicts.java`,
and all three reliably reproduced their respective bug while kept `@Disabled` (same
convention as bug 1's disabled test). `Conflicts`'s own test
(`testConcurrentRenameListLengthConflict`) never hit either gap — a same-element
rename-vs-rename conflict only exercised the update path fixed in round one.

**Fix applied** (in the `bxagent` repo, round two, 2026-07-23):
`createAndMapCTMObjectsIncremental` now builds full `Table` structure for each new
`EClass` (the `id` column, primary key, superType/root foreign key, and
attribute/reference columns) instead of just setting the name and annotations; the
deletion path now also removes the owning root table's identity column + foreign key
when a class's `Table` is deleted, closing the referential-corruption gap. All four
`Conflicts` tests pass against this fix with no assertion changes on this repo's side.

**Precondition fixtures** (`MonotonicCreatingPreEcore.ecore`/`MonotonicCreatingPreSQL.xmi`
and `MonotonicDeletingPreEcore.ecore`/`MonotonicDeletingPreSQL.xmi` in
`examples/ecoretosql/BenchmarxEcoreToSQL/resources/`) remain in place, still used as
preconditions by the now-passing tests.
