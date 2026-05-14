# BenchmarxBag1ToBag2 — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `Bag1` | `examples/bag1tobag2/metamodels/Bag1` | Source metamodel |
| `Bag2` | `examples/bag1tobag2/metamodels/Bag2` | Target metamodel |
| `BenchmarxBag1ToBag2` | `examples/bag1tobag2/BenchmarxBag1ToBag2` | Test suite |

---

## Step 1 – Parent pom `examples/bag1tobag2/pom.xml`

```xml
<modules>
  <module>metamodels/Bag1</module>
  <module>metamodels/Bag2</module>
  <module>BenchmarxBag1ToBag2</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/bag1tobag2</module>
```

---

## Step 2 – Metamodel poms (`Bag1`, `Bag2`)

Both metamodels use `src/` as source directory and depend only on `org.eclipse.emf.ecore`.
Create poms following the same pattern as `PDB1/pom.xml`.

---

## Step 3 – `BenchmarxBag1ToBag2/pom.xml`

Same structure as `BenchmarxPdb1ToPdb2/pom.xml`. Key dependencies:

```xml
<!-- Reactor siblings: Benchmarx, Bag1, Bag2 -->
<!-- EMF: ecore.xmi, emf.compare (local) -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Tool JARs (all from lib/, installed locally): -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-bag12bag2</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxlang-bag12bag2</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bx-runtime</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

---

## Step 4 – Install local JARs

Add to `install-local-deps.sh`:

```sh
LIB_BAG="<repo-root>/examples/bag1tobag2/BenchmarxBag1ToBag2/lib"

install "org.benchmarx.tools" "bxtend-bag12bag2" "1.0.0"          "${LIB_BAG}/bxtend-bag12bag2-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-bag12bag2" "1.0.0"          "${LIB_BAG}/BXtend-Bag12Bag2.jar"
install "org.benchmarx.tools" "bx-runtime"       "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "emt-agent"         "1.0.0-SNAPSHOT" "${LIB_BAG}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"         "1.0.0"          "${LIB_BAG}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"            "1.0.0"          "${LIB_BAG}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – `benchmarx.tool` filter (already applied)

`Bag12Bag2TestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tool:

| Class name | Technology |
|---|---|
| `BXAgentBags2Bags` | BX-Agent |

---

## Running the Tests

```sh
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am
./mvnw test -pl examples/bag1tobag2/BenchmarxBag1ToBag2 -am -Dbenchmarx.tool=BXAgentBags2Bags
```
