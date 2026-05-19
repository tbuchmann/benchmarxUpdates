#!/bin/sh
# install-local-deps.sh
#
# Installs JARs that are NOT on Maven Central into the local Maven repo (~/.m2).
# Run this ONCE from the repository root before the first "mvn test".
#
# Usage:  ./install-local-deps.sh
#
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN="${SCRIPT_DIR}/mvnw"
ECLIPSE_PLUGINS="/home/tb/develop/eclipse/plugins"
LIB="${SCRIPT_DIR}/examples/pdb1topdb2/BenchmarxPdb1ToPdb2/lib"

install() {
  GROUP="$1"; ARTIFACT="$2"; VERSION="$3"; JAR="$4"
  echo "Installing ${GROUP}:${ARTIFACT}:${VERSION} from ${JAR}"
  "${MVN}" install:install-file \
    -Dfile="${JAR}" \
    -DgroupId="${GROUP}" \
    -DartifactId="${ARTIFACT}" \
    -Dversion="${VERSION}" \
    -Dpackaging=jar \
    -DgeneratePom=true \
    -q
}

# ── emf.compare (not on Maven Central) ───────────────────────────────────────
EMF_COMPARE_JAR="${ECLIPSE_PLUGINS}/org.eclipse.emf.compare_3.5.3.202508281322.jar"
if [ ! -f "${EMF_COMPARE_JAR}" ]; then
  echo "ERROR: ${EMF_COMPARE_JAR} not found. Adjust ECLIPSE_PLUGINS path."
  exit 1
fi
install "org.eclipse.emf.compare" "org.eclipse.emf.compare" "3.5.3" "${EMF_COMPARE_JAR}"

# ── Tool JARs from lib/ ───────────────────────────────────────────────────────
install "org.benchmarx.tools" "bxtend-pdb12pdb2"  "1.0.0"          "${LIB}/bxtend-pdb12pdb2-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-pdb12pdb2" "1.0.0"          "${LIB}/bxagent-pdb12pdb2-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-pdb12pdb2"  "1.0.0"          "${LIB}/bxlang-pdb12pdb2.jar"
install "org.benchmarx.tools" "bx-runtime"        "1.0.0-SNAPSHOT" "${LIB}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB}/mediniQVT/qvtemf.jar"

# ── AST to DAG Tool JARs ──────────────────────────────────────────────────────
LIB_AST="${SCRIPT_DIR}/examples/asttodag/BenchmarxAstToDag/lib"

install "org.benchmarx.tools" "bxtend-ast2dag"  "1.0.0"          "${LIB_AST}/bxtend-ast2dag-1.0.0.jar"
install "org.benchmarx.tools" "bxlang-ast2dag"  "1.0.0"          "${LIB_AST}/BXtend-AST2DAG.jar"
install "org.benchmarx.tools" "emt-agent"        "1.0.0-SNAPSHOT" "${LIB_AST}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"        "1.0.0"          "${LIB_AST}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"           "1.0.0"          "${LIB_AST}/mediniQVT/qvtemf.jar"


# ── Bag1 to Bag2 Tool JARs ────────────────────────────────────────────────────
# bxtend-bag12bag2: from BenchmarxBag1ToBag2/lib/ (pre-built JAR)
LIB_BAG="${SCRIPT_DIR}/examples/bag1tobag2/BenchmarxBag1ToBag2/lib"

install "org.benchmarx.tools" "bxtend-bag12bag2"      "1.0.0"          "${LIB_BAG}/bxtend-bag12bag2-1.0.0.jar"
# bxlang-bag12bag2: CAUTION §2 – BXtend-Bag12Bag2.jar has wrong package;
#   rebuild from Bags12Bags2-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Bags12Bags2-bxlang/bin && jar cf /tmp/bxlang-bag12bag2-1.0.0.jar dev/
install "org.benchmarx.tools" "bxlang-bag12bag2"      "1.0.0"          "/tmp/bxlang-bag12bag2-1.0.0.jar"
# bxagent-bags2bags: CAUTION §2 – not in emt-agent;
#   rebuild from de.tbuchmann.bxagent.bags2bags workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/de.tbuchmann.bxagent.bags2bags/bin && jar cf /tmp/bxagent-bags2bags-1.0.0.jar de/
install "org.benchmarx.tools" "bxagent-bags2bags"     "1.0.0"          "/tmp/bxagent-bags2bags-1.0.0.jar"
install "org.benchmarx.tools" "bx-runtime"            "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "emt-agent"              "1.0.0-SNAPSHOT" "${LIB_BAG}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "plainjavaubt-bags12bags2" "1.0.0"        "${LIB_BAG}/PlainJavaUbtBags12Bags2.jar"
install "org.benchmarx.tools" "plainjavaubt-util"         "1.0.0"        "${LIB_BAG}/PlainJavaUbtUtil.jar"
install "org.benchmarx.tools" "mediniQVT"              "1.0.0"          "${LIB_BAG}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"                 "1.0.0"          "${LIB_BAG}/mediniQVT/qvtemf.jar"

# ── Set to OSet Tool JARs ─────────────────────────────────────────────────────
LIB_SET="${SCRIPT_DIR}/examples/settooset/BenchmarxSetToOSet/lib"

install "org.benchmarx.tools" "bxtend-set2oset"   "1.0.0"          "${LIB_SET}/bxtend-set2oset-1.0.0.jar"
# bxlang-set2oset: CAUTION §2 – must rebuild from Set2OSets-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Set2OSets-bxlang/bin && jar cf /tmp/bxlang-set2oset-1.0.0.jar dev/
install "org.benchmarx.tools" "bxlang-set2oset"   "1.0.0"          "/tmp/bxlang-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-set2oset"  "1.0.0"          "${LIB_SET}/bxagent-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_SET}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "bx-runtime"         "1.0.0-SNAPSHOT" "${LIB_SET}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "plainjavaubt-set2oset" "1.0.0"          "${LIB_SET}/PlainJavaUbtSet2Oset.jar"
install "org.benchmarx.tools" "plainjavaubt-util"     "1.0.0"          "${LIB_SET}/PlainJavaUbtUtil.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_SET}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_SET}/mediniQVT/qvtemf.jar"


# ── Gantt to CPM Tool JARs ────────────────────────────────────────────────────
LIB_G2C="${SCRIPT_DIR}/examples/gantttocpm/BenchmarxGanttToCPM/lib"

install "org.benchmarx.tools" "bxtend-gantt2cpm"  "1.0.0"          "${LIB_G2C}/bxtend-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-gantt2cpm" "1.0.0"          "${LIB_G2C}/bxagent-gantt2cpm-1.0.0.jar"
# bxlang-gantt2cpm: CAUTION §2 – BXtend-Gantt2CPM.jar has wrong package;
#   rebuild from Gantt2CPM-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Gantt2CPM-bxlang/bin && jar cf /tmp/bxlang-gantt2cpm-1.0.0.jar dev/
install "org.benchmarx.tools" "bxlang-gantt2cpm"  "1.0.0"          "/tmp/bxlang-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_G2C}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_G2C}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_G2C}/mediniQVT/qvtemf.jar"

echo ""
echo "All local deps installed. You can now run: ./mvnw test"
