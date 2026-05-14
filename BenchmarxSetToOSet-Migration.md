# BenchmarxSetToOSet — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `Set` | `examples/settooset/metamodels/Set` | Source metamodel |
| `OSet` | `examples/settooset/metamodels/OSet` | Target metamodel |
| `BenchmarxSetToOSet` | `examples/settooset/BenchmarxSetToOSet` | Test suite |

---

## Step 1 – Parent pom `examples/settooset/pom.xml`

```xml
<modules>
  <module>metamodels/Set</module>
  <module>metamodels/OSet</module>
  <module>BenchmarxSetToOSet</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/settooset</module>
```

---

## Step 2 – `Set/pom.xml` and `OSet/pom.xml`

Both use `src/` as source directory and depend on `org.eclipse.emf.ecore` and `Benchmarx`.
Follow the `PDB1/pom.xml` pattern.

---

## Step 3 – `BenchmarxSetToOSet/pom.xml`

```xml
<!-- Reactor siblings: Benchmarx, Set, OSet -->
<!-- EMF: ecore.xmi, emf.compare (local) -->
<!-- Guava -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Tool JARs: -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-set2oset</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxagent-set2oset</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxlang-set2oset</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

Note that `MediniQVTSetToOSet` is currently **active** in `tools()` (unlike most other
scenarios). Ensure `mediniQVT.jar` and `qvtemf.jar` are installed locally.

---

## Step 4 – Install local JARs

```sh
LIB_S2O="<repo-root>/examples/settooset/BenchmarxSetToOSet/lib"

install "org.benchmarx.tools" "bxtend-set2oset"  "1.0.0"          "${LIB_S2O}/bxtend-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-set2oset" "1.0.0"          "${LIB_S2O}/bxagent-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-set2oset"  "1.0.0"          "${LIB_S2O}/BXtend-Set2OSet.jar"
install "org.benchmarx.tools" "emt-agent"         "1.0.0-SNAPSHOT" "${LIB_S2O}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"         "1.0.0"          "${LIB_S2O}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"            "1.0.0"          "${LIB_S2O}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – `benchmarx.tool` filter (already applied)

`Set2OsetTestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tools:

| Class name | Technology |
|---|---|
| `BXtendSet2Oset` | BXtend |
| `MediniQVTSetToOSet` | MediniQVT |
| `BXLangSet2Oset` | BXLang |
| `BXAgentSet2OSet` | BX-Agent |

---

## Running the Tests

```sh
./mvnw test -pl examples/settooset/BenchmarxSetToOSet -am
./mvnw test -pl examples/settooset/BenchmarxSetToOSet -am -Dbenchmarx.tool=BXtendSet2Oset
./mvnw test -pl examples/settooset/BenchmarxSetToOSet -am -Dbenchmarx.tool=MediniQVTSetToOSet
```
