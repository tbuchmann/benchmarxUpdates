# BenchmarxFamiliesToPersons — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `Families` | `examples/familiestopersons/metamodels/Families` | Source metamodel |
| `Persons` | `examples/familiestopersons/metamodels/Persons` | Target metamodel |
| `BenchmarxFamiliesToPersons` | `examples/familiestopersons/BenchmarxFamiliesToPersons` | Test suite + scalability tests |

---

## Step 1 – Parent pom `examples/familiestopersons/pom.xml`

```xml
<modules>
  <module>metamodels/Families</module>
  <module>metamodels/Persons</module>
  <module>BenchmarxFamiliesToPersons</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/familiestopersons</module>
```

---

## Step 2 – `Families/pom.xml` and `Persons/pom.xml`

Both metamodels use `src/` as source directory and depend only on `org.eclipse.emf.ecore`
and `Benchmarx`.  Follow the same pattern as `PDB1/pom.xml`.

---

## Step 3 – `BenchmarxFamiliesToPersons/pom.xml`

This project has many optional tool implementations. The required non-Central JARs are in
`lib/`. Key dependencies:

```xml
<!-- Reactor siblings: Benchmarx, Families, Persons -->
<!-- EMF: ecore.xmi -->
<!-- Xtend runtime (required by BXtend implementations) -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Core tool JARs (always needed): -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-f2p</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bx-runtime</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent-f2p</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

> **Note on optional implementations:** Several tool wrappers (BiGUL, FunnyQT, JTL, NMF,
> eMoflon, ENeo, IBeXTGG-Integrate) require additional native or external runtimes. They are
> disabled by default (commented out in `tools()`). To activate them, add their JARs to
> `install-local-deps.sh` and uncomment them in `FamiliesToPersonsTestCase.tools()`.

---

## Step 4 – Install local JARs

```sh
LIB_F2P="<repo-root>/examples/familiestopersons/BenchmarxFamiliesToPersons/lib"

install "org.benchmarx.tools" "bxtend-f2p"    "1.0.0"          "${LIB_F2P}/bxtend-f2p-1.0.0.jar"
install "org.benchmarx.tools" "bx-runtime"    "1.0.0-SNAPSHOT" "${LIB_F2P}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "emt-agent-f2p" "1.0.0-SNAPSHOT" "${LIB_F2P}/BXAgent.jar"
install "org.benchmarx.tools" "mediniQVT"     "1.0.0"          "${LIB_F2P}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"        "1.0.0"          "${LIB_F2P}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – Scalability tests

The `scalability/` package contains `ScalabilityTests` and its subclasses. They all delegate
to `FamiliesToPersonsTestCase.tools()`, so the `-Dbenchmarx.tool` filter applies
automatically.

---

## Step 6 – `benchmarx.tool` filter (already applied)

`FamiliesToPersonsTestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tool:

| Class name | Technology |
|---|---|
| `BXAgentF2p` | BX-Agent |

Commented-out tools (require additional setup):
`UbtXtendFamiliesToPersons`, `IBeXTGGFamiliesToPersons`, `BiGULFamiliesToPersons`,
`FunnyQTFamiliesToPerson`, `NMFFamiliesToPersonsIncremental`, `JTLFamiliesToPersons`,
`EMoflonFamiliesToPersons`, `MediniQVTFamiliesToPersons`, `BXtendFamiliesToPersons`,
`BXtendDSLFamiliesToPersons`, `ENeoFamiliesToPersons`, `IBeXTGGIntegrateFamiliesToPersons`.

---

## Running the Tests

```sh
./mvnw test -pl examples/familiestopersons/BenchmarxFamiliesToPersons -am
./mvnw test -pl examples/familiestopersons/BenchmarxFamiliesToPersons -am -Dbenchmarx.tool=BXAgentF2p
```
