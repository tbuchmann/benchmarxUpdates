# Proposed fixes for BXAgent-KnownIssues.md #2 (target-side edits dropped during concurrent sync)

This document proposes concrete fixes for bug #2 in `BXAgent-KnownIssues.md` ("Target-side
edits dropped during concurrent sync, seen in Gantt2Cpm AND Set2OSet"). It supersedes that
entry's speculative "likely a shared `bx-runtime` bug" diagnosis with two independent,
code-verified root causes, found by prototyping fixes directly against the generated
transformation classes and re-running the disabled reproduction tests
(`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` in gantttocpm,
`Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict` in settooset) as the oracle.

**The root cause is not in `sync()`'s update logic at all** (Fall A/B/C, the part
`BXAgent-KnownIssues.md` pointed investigation at). It is two separate, smaller defects:

1. An adapter-wiring bug in `benchmarxUpdates` itself (`BXAgentGantt2Cpm.java` /
   `BXAgentSet2OSet.java` never call `sync()`).
2. A genuine generator-template bug in `bxagent`: `sync()` has no deletion-cascade handling.

Both must be fixed together before `performAndPropagateEdit` can safely switch from
`transform()` to `sync()` for these two examples.

## Fix 1 (benchmarxUpdates, adapter wiring): `performAndPropagateEdit` never calls `sync()`

**File**: `examples/gantttocpm/BenchmarxGanttToCPM/src/.../implementations/bxagent/BXAgentGantt2Cpm.java`
and the equivalent `BXAgentSet2OSet.java`.

Both adapters' `performAndPropagateEdit` call the plain forward-only `transform()`:

```java
// BXAgentGantt2Cpm.java, current:
public void performAndPropagateEdit(Supplier<IEdit<GanttDiagram>> sourceEdit,
        Supplier<IEdit<CPMNetwork>> targetEdit) {
    sourceEdit.get();
    targetEdit.get();
    Gantt2CpmTransformation.transform(source, target, corr,
            TransformationContext.DeletionPolicy.CASCADE, forwardHook);
}
```

`transform()` is unidirectional (source → target). It explains the entire observed bug
pattern by itself: source-side changes always propagate (that's what `transform()` does),
target-side changes are simply never read, and "conflicts" only look resolved because
forward-only `transform()` unconditionally overwrites the target from source whenever the
source changed — not because any real conflict-resolution policy ran.

For comparison, **`BXAgentEcore2SQL.performAndPropagateEdit` already calls `sync()`
correctly** and its concurrent tests are fully green (see `BXAgent-KnownIssues.md` #3,
resolved 2026-07-23). That adapter is the template to match:

```java
// BXAgentEcore2SQL.java, already correct:
public void performAndPropagateEdit(Supplier<IEdit<EPackage>> sourceEdit, Supplier<IEdit<Schema>> targetEdit) {
    sourceEdit.get();
    targetEdit.get();
    Ecore2SqlTransformation.sync(source, target, corr,
            SyncConflictPolicy.TARGET_WINS,
            TransformationContext.DeletionPolicy.CASCADE, Ecore2SqlTransformation.Options.defaults());
}
```

**Proposed fix**, verified against `Gantt2CpmTransformation.sync(...)`'s actual overloads:

```java
public void performAndPropagateEdit(Supplier<IEdit<GanttDiagram>> sourceEdit,
        Supplier<IEdit<CPMNetwork>> targetEdit) {
    sourceEdit.get();
    targetEdit.get();
    Gantt2CpmTransformation.sync(source, target, corr,
            SyncConflictPolicy.SOURCE_WINS,
            TransformationContext.DeletionPolicy.CASCADE, Gantt2CpmTransformation.Options.defaults(),
            forwardHook, backwardHook);
}
```

(analogous change in `BXAgentSet2OSet.java`, calling `Sets2OsetsTransformation.sync(...)`).

**Important detail, found only by prototyping**: it is not enough to swap the method name.
`performAndPropagateSourceEdit` passes `forwardHook` to `transform()`, and
`performAndPropagateTargetEdit` separately passes `backwardHook` to `transformBack()` — each
one-sided path only ever needed one hook. `sync()`'s 8-arg overload takes **both**
`forwardPostProcessor` and `backwardPostProcessor`. Gantt2Cpm's hooks are not decorative:
`forwardHook` creates the auxiliary `cpm.Event` nodes and wires `sourceEvent`/`targetEvent`
for new `Activity` objects (CPM has no natural counterpart for these in Gantt's model), and
`backwardHook` reconstructs `Dependency.predecessor`/`successor` on the Gantt side from CPM's
event-sharing structure. Calling `sync()` with the 6-arg overload (which defaults both hooks
to `PostProcessor.NOOP`) was tried first and immediately crashed two previously-passing tests
with `NullPointerException` (`_sourceEvent` null, `Dependency.getPredecessor()` null) — the
auxiliary Event/Dependency wiring simply never ran. Passing both hooks explicitly, as above,
eliminates those crashes.

## Fix 2 (bxagent generator repo): `sync()` has no deletion-cascade handling

Even with Fix 1 applied (both hooks passed), three previously-passing tests broke in a new
way — not crashes this time, but assertion failures showing that source-side deletions were
being **silently resurrected**: after `helperGantt::deleteGantt2CPMHelpers` deletes
`GanttHelper`/`CPMHelper` from the source, the post-sync source model still contained
`GanttHelper` with its original duration, as if the deletion had never happened.

**Root cause**: `sync()` never calls `CorrespondenceModel.findDeletedSourceEntries(...)` or
`findDeletedTargetEntries(...)` anywhere in its body — confirmed by grep against
`Gantt2CpmTransformation.java`:

```
$ grep -n "findDeletedSourceEntries(\|findDeletedTargetEntries(" Gantt2CpmTransformation.java
485:        List<EObject> _deletedFwdEntries = new ArrayList<>(CorrespondenceModel.findDeletedSourceEntries(corrResource));
681:        List<EObject> _deletedBwdEntries = new ArrayList<>(CorrespondenceModel.findDeletedTargetEntries(corrResource));
```

Both call sites are inside `transformIncremental`/`transformIncrementalBack` (the dedicated
one-sided paths) — `sync()` (lines ~1401–1666) has no equivalent. `sync()`'s only handling of
a corr entry whose source (or target) went `null` is:

- **Schritt 4** (Partition 4, both null) — a no-op cleanup that only fires once *both* sides
  are already null, which normal single-sided deletion never reaches on its own.
- **Schritt 3** (Partition 3, `src == null && tgt != null`) — this is where deletions actually
  get mishandled. When `EcoreUtil.delete()` nulls out a corr entry's source object, buildIndex's
  own doc comment says the intent is "Phase 2 will cascade-delete the target" — but `sync()`
  has no such Phase 2. Instead, Schritt 3's *own* purpose is "a new target object appeared,
  create its source counterpart" — so it treats the just-deleted `GanttHelper` entry
  (source now null, target `CPMHelper` untouched) as exactly that case, and calls
  `createNewSourceObject(_tgtObj, options)`, **recreating the object that was just deleted**.

This is a second, independent bug from Fix 1's — `sync()`'s generated code genuinely cannot
express "this object was deleted on one side, cascade-delete the other side" during a
concurrent step. It silently interprets every one-sided deletion as the opposite side's
creation instead.

**Proposed fix** (in the `bxagent` repo's code-generation template for `sync()`, not something
fixable from `benchmarxUpdates`): before Schritt 2/3 (create), `sync()` needs its own
deletion-detection pass, mirroring what `transformIncremental`/`transformIncrementalBack`
already do correctly:

1. Call `CorrespondenceModel.detectAndMarkDeletedSources(corrModel, source)` /
   the target-side equivalent, so entries whose object was deleted via
   `EcoreUtil.delete(obj, false)` (which doesn't null `eResource()`) are caught too, not just
   the `eResource() == null` case `buildIndex()` already detects directly.
2. Collect `findDeletedSourceEntries(corrModel)` / `findDeletedTargetEntries(corrModel)` up
   front, **before** Schritt 2/3 run, and cascade-delete the surviving counterpart object
   (respecting `deletionPolicy`, same as `transformIncremental`/`transformIncrementalBack`
   already do) instead of letting Schritt 2/3 treat it as a fresh creation.
3. Remove the now-handled entries from `_allEntries` (or otherwise skip them) so Schritt 2/3's
   create logic never sees them.

**Also fill in the empty "Phase 1c: Edge materialization" stub.** `sync()` in both generated
files has this exact comment with no code following it:

```
// ── Phase 1c: Edge materialization ──────────────────────────────────
// ── Schritt 6: Cross-references ──────────────────────────────────────
```

For Gantt2Cpm/Set2OSet specifically this turned out not to matter once Fix 1 passes
`forwardHook`/`backwardHook` (those hooks do the edge/cross-reference wiring for this
particular example's auxiliary Event nodes). But for any transformation that relies on
`sync()`'s own edge-materialization logic rather than a custom hook, this stub is a second,
separate empty-implementation gap of the same shape as the already-fixed Ecore2SQL bug #3
(`createAndMapCTMObjectsIncremental` producing empty stubs). Worth auditing whether other
transformations' `sync()` depend on this stub being filled in before it's assumed safe
everywhere.

## Verification methodology

Both fixes were prototyped directly against `examples/gantttocpm/BenchmarxGanttToCPM` (the
generated `Gantt2CpmTransformation.java`/adapter files are plain Java, not requiring a
`bxagent` rebuild+jar-copy cycle to test locally):

1. Baseline: `NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` (added
   in this session, `@Disabled`) fails as documented in `BXAgent-KnownIssues.md` #2a.
2. Swapped `transform()` → `sync()` in `BXAgentGantt2Cpm.performAndPropagateEdit` with the
   6-arg overload (no hooks) → 4 new test errors (`NullPointerException`), confirming hooks
   are load-bearing, not optional.
3. Same swap using the 8-arg overload (`forwardHook`, `backwardHook` both passed) → no more
   crashes; the target-edit-backward-propagation reproduction test's core assertion
   (`GanttModel` duration 0 instead of 1) now passes. But 3 *previously-passing* tests newly
   failed, all showing deleted objects reappearing → led directly to Fix 2's diagnosis.
4. Reverted both prototype changes; the two new `@Disabled` reproduction tests
   (`NonMonotonic.testConcurrentSourceDeleteHelpersTargetChangeModelDuration` in gantttocpm,
   `Conflicts.testConcurrentDeleteASrcFullRenameTrgConflict` in settooset) are what's actually
   committed — they document the bug precisely enough that re-running them after both fixes
   land upstream is a sufficient regression check (same methodology used to close bug #3).

**Recommended fix order**: land Fix 2 in the `bxagent` repo first (it's the generator-level
defect), rebuild jars, then apply Fix 1 in `benchmarxUpdates` and re-enable both disabled
tests without changing their assertions — mirroring exactly how bug #3 was closed.
