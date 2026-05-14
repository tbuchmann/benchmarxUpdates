# BenchmarxGanttToCPM — Maven Migration Guide

---

## Projects Involved

| Eclipse project | Maven module | Role |
|---|---|---|
| `Gantt` | `examples/gantttocpm/metamodels/Gantt` | Source metamodel |
| `CPM` | `examples/gantttocpm/metamodels/CPM` | Target metamodel |
| `BenchmarxGanttToCPM` | `examples/gantttocpm/BenchmarxGanttToCPM` | Test suite |

> There are two CPM metamodel projects (`CPM` and `de.ubt.ai1.m2m.cpm`). Only the canonical
> `CPM` folder is needed as a Maven module; the other is a legacy artefact.

---

## Step 1 – Parent pom `examples/gantttocpm/pom.xml`

```xml
<modules>
  <module>metamodels/Gantt</module>
  <module>metamodels/CPM</module>
  <module>BenchmarxGanttToCPM</module>
</modules>
```

Register in root `pom.xml`:
```xml
<module>examples/gantttocpm</module>
```

---

## Step 2 – `Gantt/pom.xml` and `CPM/pom.xml`

Both use `src/` as source directory and depend only on `org.eclipse.emf.ecore` and
`Benchmarx`. Follow the `PDB1/pom.xml` pattern.

---

## Step 3 – `BenchmarxGanttToCPM/pom.xml`

```xml
<!-- Reactor siblings: Benchmarx, Gantt, CPM -->
<!-- EMF: ecore, ecore.xmi, emf.compare (local) -->
<!-- Logging: log4j -->
<!-- JUnit 5 -->
<!-- Tool JARs: -->
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-gantt2cpm</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxagent-gantt2cpm</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxlang-gantt2cpm</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
<dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
```

---

## Step 4 – Install local JARs

```sh
LIB_G2C="<repo-root>/examples/gantttocpm/BenchmarxGanttToCPM/lib"

install "org.benchmarx.tools" "bxtend-gantt2cpm"  "1.0.0"          "${LIB_G2C}/bxtend-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-gantt2cpm" "1.0.0"          "${LIB_G2C}/bxagent-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-gantt2cpm"  "1.0.0"          "${LIB_G2C}/BXtend-Gantt2CPM.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_G2C}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_G2C}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_G2C}/mediniQVT/qvtemf.jar"
```

---

## Step 5 – `benchmarx.tool` filter (already applied)

`GanttToCPMTestCase.tools()` supports `-Dbenchmarx.tool=<ClassName>`. Active tool:

| Class name | Technology |
|---|---|
| `BXAgentGantt2Cpm` | BX-Agent |

---

## Running the Tests

```sh
./mvnw test -pl examples/gantttocpm/BenchmarxGanttToCPM -am
./mvnw test -pl examples/gantttocpm/BenchmarxGanttToCPM -am -Dbenchmarx.tool=BXAgentGantt2Cpm
```
