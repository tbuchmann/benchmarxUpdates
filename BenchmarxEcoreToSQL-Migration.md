# BenchmarxEcoreToSQL — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `SQL` | `examples/ecoretosql/metamodels/SQL` | Target metamodel |
| `BenchmarxEcoreToSQL` | `examples/ecoretosql/BenchmarxEcoreToSQL` | Test suite |

> The **source** of this transformation is `org.eclipse.emf.ecore.EPackage`, which is part
> of the standard EMF core — no separate source metamodel project is needed.

---

## Step 1 – Parent pom `examples/ecoretosql/pom.xml`

```xml
<modules>
  <module>metamodels/SQL</module>
  <module>BenchmarxEcoreToSQL</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/ecoretosql</module>
```

---

## Step 2 – `SQL/pom.xml`

`SQL` depends on EMF ecore and uses `src/` as source directory. The metamodel also uses
Xtend-generated source in `xtend-gen/` — add a `build-helper-maven-plugin` execution to
include it (same pattern as `PDB1/pom.xml`).

```xml
<dependency><groupId>org.eclipse.emf</groupId><artifactId>org.eclipse.emf.ecore</artifactId></dependency>
<dependency><groupId>org.eclipse.xtend</groupId><artifactId>org.eclipse.xtend.lib</artifactId></dependency>
<dependency><groupId>org.eclipse.xtext</groupId><artifactId>org.eclipse.xtext.xbase.lib</artifactId></dependency>
<dependency><groupId>com.google.guava</groupId><artifactId>guava</artifactId></dependency>
```

---

## Step 3 – `BenchmarxEcoreToSQL/pom.xml`

This project has `xtend-gen/` sources as well. Add both `src/` and `xtend-gen/` via `build-helper-maven-plugin`.

Key dependencies:

```xml
<!-- Reactor siblings: Benchmarx, SQL -->
<!-- EMF: ecore, ecore.xmi, emf.compare (local) -->
<!-- Xtend runtime -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Tool JARs: -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-ecore2sql</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxagent-ecore2sql</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

---

## Step 4 – Install local JARs

```sh
LIB_E2S="<repo-root>/examples/ecoretosql/BenchmarxEcoreToSQL/lib"

install "org.benchmarx.tools" "bxtend-ecore2sql"  "1.0.0"          "${LIB_E2S}/bxtend-ecore2sql-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-ecore2sql" "1.0.0"          "${LIB_E2S}/bxagent-ecore2sql-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_E2S}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_E2S}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_E2S}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – `benchmarx.tool` filter (already applied)

`EcoreToSQLTestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tool:

| Class name | Technology |
|---|---|
| `BXAgentEcore2SQL` | BX-Agent |

---

## Running the Tests

```sh
./mvnw test -pl examples/ecoretosql/BenchmarxEcoreToSQL -am
./mvnw test -pl examples/ecoretosql/BenchmarxEcoreToSQL -am -Dbenchmarx.tool=BXAgentEcore2SQL
```
