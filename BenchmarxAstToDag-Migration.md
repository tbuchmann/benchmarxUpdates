# BenchmarxAstToDag — Maven Migration Guide

This document describes the steps required to migrate the `BenchmarxAstToDag`
Eclipse plug-in project (and its metamodel plug-ins) to a plain Maven build,
following the same approach used for `BenchmarxPdb1ToPdb2`.

---

## Projects Involved

| Eclipse project | Becomes Maven module | Role |
|---|---|---|
| `ExpressionAST` | `examples/asttodag/metamodels/ExpressionAST` | Source metamodel |
| `ExpressionDAG` | `examples/asttodag/metamodels/ExpressionDAG` | Target metamodel |
| `BenchmarxAstToDag` | `examples/asttodag/BenchmarxAstToDag` | Test suite |

---

## Step 1 – Add a parent `pom.xml` for the example group

Create `examples/asttodag/pom.xml`:

```xml
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.benchmarx</groupId>
    <artifactId>benchmarxUpdates-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>
  <artifactId>asttodag-parent</artifactId>
  <packaging>pom</packaging>
  <name>BenchmarX - AST to DAG (Parent)</name>
  <modules>
    <module>metamodels/ExpressionAST</module>
    <module>metamodels/ExpressionDAG</module>
    <module>BenchmarxAstToDag</module>
  </modules>
</project>
```

Register this parent in the root `pom.xml` `<modules>` section:
```xml
<module>examples/asttodag</module>
```

---

## Step 2 – `ExpressionAST/pom.xml`

```xml
<project ...>
  <parent>...</parent>  <!-- asttodag-parent -->
  <artifactId>ExpressionAST</artifactId>
  <packaging>jar</packaging>
  <build><sourceDirectory>src</sourceDirectory></build>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.emf</groupId><artifactId>org.eclipse.emf.ecore</artifactId>
    </dependency>
  </dependencies>
</project>
```

## Step 3 – `ExpressionDAG/pom.xml`

Same structure as `ExpressionAST` above but `<artifactId>ExpressionDAG</artifactId>`.

---

## Step 4 – `BenchmarxAstToDag/pom.xml`

Key points:
- `<sourceDirectory>src</sourceDirectory>` (Eclipse convention)
- `<testClassesDirectory>${project.build.outputDirectory}</testClassesDirectory>` in Surefire config
- All tool JARs from `lib/` must be installed locally first (see Step 5)

```xml
<dependencies>
  <!-- Reactor siblings -->
  <dependency><groupId>org.benchmarx</groupId><artifactId>Benchmarx</artifactId><version>${project.version}</version></dependency>
  <dependency><groupId>org.benchmarx</groupId><artifactId>ExpressionAST</artifactId><version>${project.version}</version></dependency>
  <dependency><groupId>org.benchmarx</groupId><artifactId>ExpressionDAG</artifactId><version>${project.version}</version></dependency>
  <!-- EMF -->
  <dependency><groupId>org.eclipse.emf</groupId><artifactId>org.eclipse.emf.ecore.xmi</artifactId></dependency>
  <!-- emf.compare – installed locally, see Step 5 -->
  <dependency><groupId>org.eclipse.emf.compare</groupId><artifactId>org.eclipse.emf.compare</artifactId><version>3.5.3</version></dependency>
  <!-- Logging -->
  <dependency><groupId>log4j</groupId><artifactId>log4j</artifactId></dependency>
  <!-- JUnit 5 -->
  <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId></dependency>
  <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-params</artifactId></dependency>
  <!-- Tool JARs – installed locally, see Step 5 -->
  <dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxtend-ast2dag</artifactId><version>1.0.0</version></dependency>
  <dependency><groupId>org.benchmarx.tools</groupId><artifactId>bxlang-ast2dag</artifactId><version>1.0.0</version></dependency>
  <dependency><groupId>org.benchmarx.tools</groupId><artifactId>emt-agent</artifactId><version>1.0.0-SNAPSHOT</version></dependency>
  <dependency><groupId>org.benchmarx.tools</groupId><artifactId>mediniQVT</artifactId><version>1.0.0</version></dependency>
  <dependency><groupId>org.benchmarx.tools</groupId><artifactId>qvtemf</artifactId><version>1.0.0</version></dependency>
</dependencies>
```

---

## Step 5 – Install local JARs

Add the following entries to `install-local-deps.sh` in the repository root:

```sh
LIB_AST="<repo-root>/examples/asttodag/BenchmarxAstToDag/lib"

install "org.benchmarx.tools" "bxtend-ast2dag"  "1.0.0"          "${LIB_AST}/bxtend-ast2dag-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-ast2dag"  "1.0.0"          "${LIB_AST}/BXtend-AST2DAG.jar"
install "org.benchmarx.tools" "emt-agent"        "1.0.0-SNAPSHOT" "${LIB_AST}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"        "1.0.0"          "${LIB_AST}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"           "1.0.0"          "${LIB_AST}/mediniQVT/qvtemf.jar"
```

(`emf.compare` is shared and already installed by the pdb1topdb2 section.)

---

## Step 6 – `benchmarx.tool` filter (already applied)

`Ast2DagTestCase.tools()` has been updated to support the `-Dbenchmarx.tool=<ClassName>` system property. Active tools:

| Class name | Technology |
|---|---|
| `BXtendAst2Dag` | BXtend |
| `BXLangAst2Dag` | BXLang |
| `BXAgentAst2Dag` | BX-Agent |

---

## Running the Tests

```sh
# All active tools
./mvnw test -pl examples/asttodag/BenchmarxAstToDag -am

# Single tool
./mvnw test -pl examples/asttodag/BenchmarxAstToDag -am -Dbenchmarx.tool=BXAgentAst2Dag
```
