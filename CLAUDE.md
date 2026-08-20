# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```sh
# Build everything
./mvnw install

# Run all tests for one example
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am

# Run tests for a specific BX tool implementation
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags

# Run a single test class
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags -Dtest=BatchForward

# Run a single test method
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags -Dtest=BatchForward#testCreateOneBeer

# Generate HTML test report
mvn surefire-report:report
```

No setup step is required before running tests: every non-Maven-Central JAR (BX tool
JARs, emf.compare) is already checked into the relevant example's `lib/` folder and
resolvable via the committed `repo/` Maven repository (a `<repositories>` entry in the
root `pom.xml`, populated by symlinks back into `lib/`). `vendor-deps.sh` only needs to
be re-run when adding a brand-new non-Central JAR — see README.md.

## Architecture

**Benchmarx** is a framework for benchmarking bidirectional transformation (BX) tools. It defines a uniform test suite that multiple BX tool implementations must pass.

### Core Framework (`core/Benchmarx/`)

The core provides abstractions only — no example-specific code.

#### `BXTool<S, T, D>`
The central interface every BX tool adapter must implement. Type parameters: `S` = source model root type, `T` = target model root type, `D` = decisions enum (for non-deterministic propagation choices).

Key methods:
- `initiateSynchronisationDialogue()` — called once before a test; tool sets up empty models, correspondence model, and an initial consistent state.
- `terminateSynchronisationDialogue()` — called once after a test; cleanup (default no-op).
- `performAndPropagateEdit(srcEdit, trgEdit)` — concurrent sync: both edits happen, tool resolves conflicts.
- `performAndPropagateSourceEdit(edit)` / `performAndPropagateTargetEdit(edit)` — one-sided propagation (default impls delegate to `performAndPropagateEdit` with an idle edit on the other side).
- `performIdleSourceEdit(edit)` / `performIdleTargetEdit(edit)` — apply an edit without triggering propagation (used to set up preconditions without testing propagation in the wrong direction).
- `assertPrecondition(S, T)` / `assertPostcondition(S, T)` — compare the tool's live models against expected EMF objects.
- `setConfigurator(Configurator<D>)` — inject a decision policy before propagation.
- `getSourceModel()` / `getTargetModel()` — expose the tool's live model roots.

#### `BXToolForEMF<S, T, D>`
Abstract class implementing `BXTool` for EMF-based tools. Constructed with two `BiConsumer<M, M>` comparators (one per model). Implements `assertPrecondition`/`assertPostcondition` by calling the comparators. Also declares `saveModels(String name)` for debug snapshots. Implements idle edits as plain `edit.get()` (no propagation).

#### Edit model (`org.benchmarx.edit`)
Edits are recorded as lists of `AtomicEdit<M>` steps and passed to the tool as `Supplier<IEdit<M>>` lambdas so the tool controls when the edit is actually applied.

- **`IEdit<M>`** — interface with `getSteps()` and `andThen(IEdit)`.
- **`Edit<M>`** — mutable list-backed implementation.
- **`IdleEdit<M>`** — empty edit (no steps); returned by `IEdit.idleEdit()`.
- **`AtomicEdit<M>`** subtypes: `CreateNode`, `DeleteNode`, `CreateEdge`, `DeleteEdge`, `ChangeAttribute`, `MoveNode`.

The test case base class collects atomic edits into an `Edit<M>` instance via the helper callbacks (see below), then passes the whole `Edit` to the tool as a `Supplier`.

#### `Configurator<D>`
A simple boolean decision map. Before a propagation step that is non-deterministic (e.g. "should the new person become a parent or a child?"), the test sets decisions via `configurator.makeDecision(D, boolean)`, then calls `tool.setConfigurator(configurator)`. During propagation the tool calls `configurator.decide(D)` to retrieve the policy. Throws `IllegalArgumentException` if a decision was not registered, which catches unexpected decision requests early.

#### `BenchmarxUtil<S, T, D>`
A thin helper held by each test case. Wraps the tool to provide:
- `assertPrecondition(String, String)` / `assertPostcondition(String, String)` — loads XMI files from `resources/<name>.xmi` on the classpath and delegates to `tool.assertPrecondition/assertPostcondition`.
- `assertAnyPostcondition(Map<String,String>)` — tries each (src, trg) XMI pair in turn; passes if any one holds. Used for `Conflicts` tests where multiple valid resolutions exist.
- `configure()` — creates a fresh `Configurator<D>`, sets it on the tool, and returns it for chaining.

### Example Structure

Each example lives under `examples/<name>/` and follows this layout:
```
examples/<name>/
  pom.xml                          (example group parent POM)
  metamodels/
    <SrcModel>/pom.xml             (EMF metamodel JAR)
    <TrgModel>/pom.xml
  Benchmarx<Name>/
    pom.xml                        (test module)
    src/org/benchmarx/examples/<name>/
      testsuite/
        <Name>TestCase.java        (abstract base; wires helpers, lifecycle, tools())
        BXToolParameterResolver.java  (per-example JUnit 5 ParameterResolver)
        Decisions.java             (enum of non-deterministic choices for this example)
        batch/fwd/BatchForward.java
        batch/bwd/BatchBackward.java
        alignment_based/fwd/IncrementalForward.java
        alignment_based/bwd/IncrementalBackward.java
        alignment_based/roundtrip/RoundtripTests.java
        concurrent/MonotonicCreating.java
        concurrent/MonotonicDeleting.java
        concurrent/NonMonotonic.java
        concurrent/Conflicts.java
      implementations/
        bxagent/BXAgent<Name>.java
      helpers/                     (in metamodel modules, not in test module)
        <Src>Helper.java           (named edit operations for test bodies)
        <Trg>Helper.java
    resources/                     (XMI fixture files for expected model states)
    lib/                           (local BX tool JARs, vendored via root vendor-deps.sh)
```

#### `<Name>TestCase` — per-example test base class
Not a generic core class. Each example defines its own. Responsibilities:
- Holds `tool`, `util` (`BenchmarxUtil`), and the two `Helper` instances as protected fields.
- `@BeforeEach initialise()`: registers EMF packages, instantiates comparators, creates `BenchmarxUtil`, calls `tool.initiateSynchronisationDialogue()`, wires the helpers (passing lambdas that append to the current `Edit` accumulator).
- `@AfterEach terminate()`: calls `tool.terminateSynchronisationDialogue()`.
- `static tools()`: returns the list of `BXTool` instances to test; filters by `System.getProperty("benchmarx.tool")` when set.
- `srcEdit(Runnable... ops)` / `trgEdit(Runnable... ops)`: creates a fresh `Edit<M>`, runs each `Runnable` (which appends `AtomicEdit` steps to it via the helper callbacks), and returns the edit as a `Supplier`.

#### `BXToolParameterResolver` — per-example JUnit 5 extension
Resolves the `BXTool` constructor parameter for `@ParameterizedTest` classes. It calls `<Name>TestCase.tools()`, parses the 1-based invocation index from JUnit's display name (e.g. `[1] BXagent`), and returns the tool at that index. Registered on each concrete test class via `@ExtendWith(BXToolParameterResolver.class)`.

#### `Decisions` enum
An example-specific enum listing all choices the tool may need at runtime. The `Configurator<D>` maps each enum constant to a `boolean`. Example for FamiliesToPersons:
```java
enum Decisions {
    PREFER_CREATING_PARENT_TO_CHILD,
    PREFER_EXISTING_FAMILY_TO_NEW
}
```

#### `*Helper` classes
Provide named, domain-level edit operations (e.g. `helperFamily.createSimpsonFamily()`). Each operation appends the corresponding `AtomicEdit` steps to the current `Edit` accumulator via callbacks injected during `initialise()`. Helpers live in the metamodel Maven module (e.g. `metamodels/Families/src/…/FamilyHelper.java`), not in the test module.

#### `*Comparator` classes
`BiConsumer<M, M>` implementations that compare two EMF model roots using `EcoreUtil.equals` and throw a JUnit 5 `AssertionFailedError` on mismatch. Must use `org.junit.jupiter.api.Assertions` — **not** the JUnit 4 `org.junit.Assert` (a known migration issue in older comparators).

### EMF / Models

- All models are EMF-based. Metamodels are `.ecore` files generated into Java, packaged as Maven JARs.
- Expected pre/postcondition states are XMI files in `resources/`; loaded by `BenchmarxUtil` via `EMFUtil.loadExpectedModel(name)` which looks up `resources/<name>.xmi` on the classpath.
- `*.corr.xmi` files are correspondence/alignment models maintained by the tool across propagation steps.

### Test Categories

Surefire is configured to pick up: `Batch*`, `Incremental*`, `Roundtrip*`, `Conflicts`, `MonotonicCreating`, `MonotonicDeleting`, `NonMonotonic`.

### Concrete Example: FamiliesToPersons

The transformation synchronises a `FamilyRegister` (families with members tagged as father/mother/son/daughter) with a `PersonRegister` (males and females with full names).

**Roles of each type:**

| Type | Role |
|---|---|
| `FamiliesToPersonsTestCase` | Base test class; wires `FamilyHelper`, `PersonHelper`, `FamiliesComparator`, `PersonsComparator`, `BenchmarxUtil` |
| `Decisions` | `PREFER_CREATING_PARENT_TO_CHILD`, `PREFER_EXISTING_FAMILY_TO_NEW` |
| `BXAgentF2p extends BXToolForEMF` | Adapter for the BXAgent tool; owns three EMF `Resource`s (source `.family`, target `.person`, correspondence `.corr`) |
| `FamilyHelper` / `PersonHelper` | Named operations: `createSimpsonFamily()`, `createSonRod()`, `renameEmptySimpsonToBouvier()`, etc. |
| `BatchForward` | Concrete test class; `@ExtendWith(BXToolParameterResolver.class)`, `@ParameterizedTest @MethodSource("tools")` |

**Flow of a single test (e.g. `testFamilyNameChangeOfEmpty`):**
```
@BeforeEach initialise()
  → tool.initiateSynchronisationDialogue()   // BXAgentF2p: creates empty resources, runs initial batch-fwd

tool.performAndPropagateSourceEdit(
    srcEdit(helperFamily::createSimpsonFamily)  // builds Edit<FamilyRegister> with CreateNode + CreateEdge steps
)                                              // BXAgentF2p: calls edit.get(), then Families2PersonsTransformation.transform(src, tgt, corr, ...)

util.assertPrecondition("Pre_NameChangeFamilyEmpty", "Pre_NameChangePersonEmpty")
  // loads resources/Pre_NameChangeFamilyEmpty.xmi, calls tool.assertPrecondition(expected, actual)
  // BXToolForEMF: delegates to FamiliesComparator + PersonsComparator

tool.performAndPropagateSourceEdit(srcEdit(helperFamily::renameEmptySimpsonToBouvier))

util.assertPostcondition("NameChangeFamilyEmpty", "NameChangePersonEmpty")

@AfterEach terminate()
  → tool.terminateSynchronisationDialogue()
```

**Concurrent sync (`performAndPropagateEdit`):** Both source and target edits are applied first, then `Families2PersonsTransformation.sync(src, tgt, corr, SyncConflictPolicy.TARGET_WINS, ...)` resolves conflicts. `Conflicts` tests use `util.assertAnyPostcondition(map)` to accept multiple valid resolutions.

**Adding a new BX tool implementation:** implement `BXToolForEMF<FamilyRegister, PersonRegister, Decisions>`, register it in `FamiliesToPersonsTestCase.tools()`, and add the tool's JAR to `lib/` + `vendor-deps.sh`.

## Module Registration Status

All examples are now registered in the root `pom.xml`: `core/Benchmarx`, `examples/pdb1topdb2`, `examples/asttodag`, `examples/bag1tobag2`, `examples/settooset`, `examples/gantttocpm`, `examples/pntopnw`, `examples/familiestopersons`, `examples/ecoretosql`.

Some tool implementations within `familiestopersons` and `ecoretosql` (the `ibextgg`/eMoflon-based ones) are still excluded from the Maven build pending a full IBeX-TGG Eclipse setup — see each module's `CAUTION.md`. `examples/containerstominiyaml` exists in the repo but is not yet a registered Maven module.

## Migration Notes

When migrating an example to Maven:
1. Create a parent POM for the example group and register it in the root `pom.xml`.
2. Create `pom.xml` for each metamodel submodule.
3. Create the test module `pom.xml` with dependencies on `core/Benchmarx`, metamodel JARs, and tool JARs.
4. Add `vendor "<groupId>" "<artifactId>" "<version>" "<jar>"` entries for the new example's non-Central JARs to the root-level `vendor-deps.sh` (uses `mvn install:install-file` under the hood, targeting the committed `repo/` — one script covers all examples).
5. Fix any JUnit 4 `Assert` imports in comparator classes → JUnit 5 `Assertions`.
6. Add a `CAUTION.md` documenting known issues (mismatched JAR packages, pre-existing tool failures).

See `Discussion.md` at the repo root and per-example `INSTRUCTIONS.md` / `CAUTION.md` for detailed guidance.
