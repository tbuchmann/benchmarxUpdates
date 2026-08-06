# benchmarx

Infrastructure for implementing benchmarx: benchmarks for bidirectional transformation (bx) tools. Also contains a collection of example benchmarx and test runners for various and diverse bx tools.

The project was originally a collection of standalone Eclipse plug-in projects (driven via PSF team project sets and run as Eclipse JUnit launches). It has since been ported to a single plain **Maven** multi-module build (JDK 17, JUnit 5). This README documents the current Maven-based workflow. The old Eclipse-only workflow is kept at the bottom for historical reference.

## Repository layout

```
core/Benchmarx/                  core abstractions (BXTool, edits, Configurator, ...)
examples/<name>/
  pom.xml                        example parent POM
  metamodels/<Src>/pom.xml       EMF metamodel module(s)
  metamodels/<Trg>/pom.xml
  Benchmarx<Name>/pom.xml        test module (test suite + tool implementations)
    src/org/benchmarx/examples/<name>/
      testsuite/                 test cases, TestCase base class, Decisions enum
      implementations/           one package per bx tool
    resources/                   expected pre-/postcondition .xmi files
    lib/                         pre-built tool JARs not available on Maven Central
install-local-deps.sh            one-time script: installs all non-Central JARs into ~/.m2
```

Registered example modules (root `pom.xml`): `pdb1topdb2`, `asttodag`, `bag1tobag2`, `settooset`, `gantttocpm`, `pntopnw`, `familiestopersons`, `ecoretosql`. See `CLAUDE.md` for the detailed architecture (interfaces, edit model, test lifecycle).

## One-time setup

1. Install a JDK 17 and make sure `./mvnw` works (`./mvnw -v`).
2. From the repository root, install the JARs that aren't on Maven Central into your local `~/.m2` repo:
   ```sh
   ./install-local-deps.sh
   ```
   This installs `org.eclipse.emf.compare` (from a local Eclipse install — adjust `ECLIPSE_PLUGINS` in the script if yours differs) plus the pre-built tool JARs for every example (from each example's `lib/` folder). **Caveat:** a few entries install from files under `/tmp/*.jar` that are not checked into the repo — the script's comments explain how to rebuild them from the corresponding Eclipse workspace project (e.g. a `bxlang-*` or `bxagent-*` JAR). Until those JARs are rebuilt and placed at the expected `/tmp` path, the affected tool implementations for that example won't compile/run; every other tool in that example is unaffected. This is a known gap — see "Known limitations" below.
3. Run `./install-local-deps.sh` again any time a new example/tool is added with its own non-Central dependencies (it's idempotent).

## Running tests

```sh
# Build + run everything (all registered examples, all tools)
./mvnw install

# Run all tools for one example
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am

# Run one specific tool implementation for one example
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags

# Run a single test class
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags -Dtest=BatchForward

# Run a single test method
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags -Dtest=BatchForward#testCreateOneBeer

# HTML test report (after a test run)
mvn surefire-report:report
```

`-pl <module> -am` selects one example's test module and also builds its dependencies (core + its metamodels). `-Dbenchmarx.tool=<SimpleClassName>` filters to a single tool implementation — the class name must match `Class#getSimpleName()` of one of the entries returned by that example's `tools()` method (e.g. `examples/bag1tobag2/.../Bag12Bag2TestCase.java`); omit it to run every tool registered there. Surefire only picks up classes matching `Batch*`, `Incremental*`, `Roundtrip*`, `Conflicts`, `MonotonicCreating`, `MonotonicDeleting`, `NonMonotonic` (configured in the root `pom.xml`).

## Adding a new BX tool implementation to an existing example

Taking `BenchmarxBag1ToBag2` as an example, substitute as appropriate:

1. Add a package for your tool under `examples/bag1tobag2/BenchmarxBag1ToBag2/src/org/benchmarx/examples/bag12bag2/implementations/<your_bx_tool>/` and implement `BXToolForEMF<S, T, Decisions>` (see `core/Benchmarx` and existing implementations for the pattern).
2. If your tool ships as a pre-built JAR not on Maven Central, drop it into that example's `lib/` folder, add a Maven `<dependency>` for it in `Benchmarx<Name>/pom.xml`, and add a matching `install "<groupId>" "<artifactId>" "<version>" "<jar path>"` line to the root `install-local-deps.sh`.
3. Register your new tool class in the example's `tools()` method (e.g. `Bag12Bag2TestCase.tools()`), which both the default `mvn test` run and the `-Dbenchmarx.tool=...` filter use.
4. Run `./install-local-deps.sh` again, then `./mvnw test -pl examples/<name>/Benchmarx<Name> -am -Dbenchmarx.tool=<YourToolClassName>` to exercise just your tool.
5. Feel free to add new JUnit tests that demonstrate the strengths of your tool. Classify new tests using the existing categories (`Batch*`, `Incremental*`, `Roundtrip*`, `concurrent/*`) — see `examples/ecoretosql/BenchmarxEcoreToSQL/INSTRUCTIONS.md` for a worked template on writing roundtrip/concurrent tests.
6. If a comparator/helper class you touch still imports JUnit 4's `org.junit.Assert`, replace it with plain `if (...) throw new AssertionError(...)` — see any example's `CAUTION.md` for the established pattern.

## Adding a new example

1. Copy the structure of an existing example (e.g. `examples/bag1tobag2`): an example parent `pom.xml`, `metamodels/<Src>/pom.xml` + `metamodels/<Trg>/pom.xml` (EMF-generated code, packaged as a Maven module — add the `build-helper-maven-plugin` source-folder config shown in `metamodels/Bag1/pom.xml` if generated code lives outside `src/`), and a `Benchmarx<Name>/pom.xml` test module depending on `core/Benchmarx` and both metamodel modules.
2. Write the `<Name>TestCase` base class, `Decisions` enum, `BXToolParameterResolver`, comparators, and helpers, following the pattern described in `CLAUDE.md`.
3. Register the example's parent module in the root `pom.xml`'s `<modules>` list.
4. Add any non-Central tool JAR installs to `install-local-deps.sh`.
5. Document known migration/porting issues for the example in a `CAUTION.md` inside the test module (see existing examples for the format).

## Known limitations

- `install-local-deps.sh` references a local Eclipse installation path (`ECLIPSE_PLUGINS`) for `org.eclipse.emf.compare`, which must still be adjusted per machine. The handful of entries that used to require rebuilding a JAR from an Eclipse workspace under `.local-build/` are now superseded by the current `de.tbuchmann.bxagent:de-tbuchmann-bxagent-<name>` / `dev.bxagent:bx-runtime` jars checked into each example's `lib/`, and are commented out in the script — every other JAR the script installs comes straight from a `lib/` folder checked into the repo, so a clean checkout only needs the `emf.compare` path fixed up.
- `BenchmarxFamiliesToPersons` and `BenchmarxEcoreToSQL` still have `ibextgg`/eMoflon-based implementations excluded from the Maven build (require a full IBeX-TGG Eclipse setup); see each module's `CAUTION.md`.
- Per-example migration write-ups (what changed and why when porting each Eclipse project to Maven) live at the repo root: `BenchmarxAstToDag-Migration.md`, `BenchmarxBag1ToBag2-Migration.md` (+ `-MigrationPlan.md`), `BenchmarxEcoreToSQL-Migration.md`, `BenchmarxFamiliesToPersons-Migration.md`, `BenchmarxGanttToCPM-Migration.md`, `BenchmarxPetrinetToPetrinetWeighted-Migration.md`, `BenchmarxSetToOSet-Migration.md`.

---

## Legacy: running the benchmarx the original (Eclipse) way

This section describes the pre-Maven workflow. It's kept for historical reference; new work should use the Maven workflow above.

We have a plug and play (via remote desktop) Share virtual machine available from: http://is.ieis.tue.nl/staff/pvgorp/share/?page=ConfigureNewSession&vdi=Ubuntu12LTS_BenchmarX.vdi

1. Clone this repo: `git clone https://github.com/eMoflon/benchmarx.git benchmarx`
2. Download the latest version of the **Eclipse Modeling Tools** for your platform.
3. Start Eclipse in a workspace of your choice and install Xtend (e.g., using `Help/Eclipse market place`). Restart and import the PSF file (`Import/Team/Team Project Set`) in the benchmark example folder you're interested in. As an example, for **BenchmarxFamiliesToPersons** this would be (`benchmarx/examples/familiestopersons/projectSet.psf`). If your workspace does not compile then please create an issue for us.
   For all examples, make sure you switch to `UTF 8` as encoding for your Eclipse workspace (`Window/Preferences/General/Workspace/Text file encoding`).
4. Choose the tools you want to execute by appropriately manipulating (replace with your benchmark example) `/BenchmarxFamiliesToPersons/src/org/benchmarx/examples/familiestopersons/testsuite/FamiliesToPersonsTestCase.java/tools()`.
5. (Optional) Some bx tools (including BiGUL and NMF) are commented out per default as they require some additional setup. See the referenced READMEs in the `FamiliesToPersonsTestCase.java/tools()` for the necessary steps. As of 10.05.2023, FunnyQT no longer works, probably due to compatibility reasons with the current Java version.
6. Choose the project , e.g. **BenchmarxFamiliesToPersons**, and select "Run As/JUnit Test" to execute the benchmarx "Families to Persons" for all tools chosen in Step 5 (replace with your benchmark example).
7. You can compare your results with `/BenchmarxFamiliesToPersons/results/TestResults.xlsx`.
8. For scalability tests, you can edit and execute `/BenchmarxFamiliesToPersons/src/org/benchmarx/examples/familiestopersons/testsuite/scalability/ScalabilityMeasurements.java` as required. The referenced tools should of course be setup and working as expected in Step 4/5/6.
