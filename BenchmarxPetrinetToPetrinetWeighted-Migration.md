# BenchmarxPetrinetToPetrinetWeighted — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `Petrinet` | `examples/pntopnw/metamodels/Petrinet` | Source metamodel |
| `PetrinetWeighted` | `examples/pntopnw/metamodels/PetrinetWeighted` | Target metamodel |
| `BenchmarxPetrinetToPetrinetWeighted` | `examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted` | Test suite |

---

## Step 1 – Parent pom `examples/pntopnw/pom.xml`

```xml
<modules>
  <module>metamodels/Petrinet</module>
  <module>metamodels/PetrinetWeighted</module>
  <module>BenchmarxPetrinetToPetrinetWeighted</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/pntopnw</module>
```

---

## Step 2 – `Petrinet/pom.xml` and `PetrinetWeighted/pom.xml`

Both use `src/` as source directory. They depend on `org.eclipse.emf.ecore` and
`Benchmarx`. Follow the `PDB1/pom.xml` pattern.

---

## Step 3 – `BenchmarxPetrinetToPetrinetWeighted/pom.xml`

```xml
<!-- Reactor siblings: Benchmarx, Petrinet, PetrinetWeighted -->
<!-- EMF: ecore.xmi, emf.compare (local) -->
<!-- Guava (pulled in by some tool impls) -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Tool JARs: -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-pn2pnw</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent-pn2pnw</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

---

## Step 4 – Install local JARs

```sh
LIB_PN="<repo-root>/examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted/lib"

install "org.benchmarx.tools" "bxtend-pn2pnw"    "1.0.0"          "${LIB_PN}/bxtend-pn2pnw-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent-pn2pnw" "1.0.0-SNAPSHOT" "${LIB_PN}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"         "1.0.0"          "${LIB_PN}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"            "1.0.0"          "${LIB_PN}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – `benchmarx.tool` filter (already applied)

`Pn2PnwTestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tools:

| Class name | Technology |
|---|---|
| `BXtendPn2Pnw` | BXtend |
| `BXAgentPn2Pnw` | BX-Agent |

---

## Running the Tests

```sh
./mvnw test -pl examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted -am
./mvnw test -pl examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted -am -Dbenchmarx.tool=BXtendPn2Pnw
```
