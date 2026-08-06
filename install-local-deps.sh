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

# Jars that must be manually rebuilt from an Eclipse workspace project (see the
# CAUTION.md files) go here instead of a machine-specific /tmp path, so the
# build step survives reboots and works the same on every checkout.
BUILD_TMP="${SCRIPT_DIR}/.local-build"
mkdir -p "${BUILD_TMP}"

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
# bxlang-pdb12pdb2: BXLang excluded for now (see per-example TestCase.java tools()).
# install "org.benchmarx.tools" "bxlang-pdb12pdb2"  "1.0.0"          "${LIB}/bxlang-pdb12pdb2.jar"
install "org.benchmarx.tools" "bx-runtime"        "1.0.0-SNAPSHOT" "${LIB}/bx-runtime-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-pdb12pdb2 / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxPdb1ToPdb2/pom.xml depends on (supersedes the org.benchmarx.tools:* jars above).
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-pdb12pdb2" "1.0.0-SNAPSHOT" "${LIB}/de-tbuchmann-bxagent-pdb12pdb2-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB}/mediniQVT/qvtemf.jar"

# ── AST to DAG Tool JARs ──────────────────────────────────────────────────────
LIB_AST="${SCRIPT_DIR}/examples/asttodag/BenchmarxAstToDag/lib"

install "org.benchmarx.tools" "bxtend-ast2dag"  "1.0.0"          "${LIB_AST}/bxtend-ast2dag-1.0.0.jar"
# bxlang-ast2dag: BXLang excluded for now (see per-example TestCase.java tools()).
# install "org.benchmarx.tools" "bxlang-ast2dag"  "1.0.0"          "${LIB_AST}/BXtend-AST2DAG.jar"
install "org.benchmarx.tools" "emt-agent"        "1.0.0-SNAPSHOT" "${LIB_AST}/emt-agent-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-ast2dag / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxAstToDag/pom.xml depends on.
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-ast2dag" "1.0.0-SNAPSHOT" "${LIB_AST}/de-tbuchmann-bxagent-ast2dag-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                   "1.0.0-SNAPSHOT" "${LIB_AST}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"        "1.0.0"          "${LIB_AST}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"           "1.0.0"          "${LIB_AST}/mediniQVT/qvtemf.jar"


# ── Bag1 to Bag2 Tool JARs ────────────────────────────────────────────────────
# bxtend-bag12bag2: from BenchmarxBag1ToBag2/lib/ (pre-built JAR)
LIB_BAG="${SCRIPT_DIR}/examples/bag1tobag2/BenchmarxBag1ToBag2/lib"

install "org.benchmarx.tools" "bxtend-bag12bag2"      "1.0.0"          "${LIB_BAG}/bxtend-bag12bag2-1.0.0.jar"
# bxlang-bag12bag2: BXLang excluded for now (see per-example TestCase.java tools()).
#   CAUTION §2 – BXtend-Bag12Bag2.jar has wrong package; if re-enabled, rebuild from
#   Bags12Bags2-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Bags12Bags2-bxlang/bin && jar cf ${BUILD_TMP}/bxlang-bag12bag2-1.0.0.jar dev/
# install "org.benchmarx.tools" "bxlang-bag12bag2"      "1.0.0"          "${BUILD_TMP}/bxlang-bag12bag2-1.0.0.jar"
# bxagent-bags2bags: obsolete, no pom.xml depends on it anymore (superseded by
# de-tbuchmann-bxagent-bag12bag2 below); kept commented out since its jar must be
# manually rebuilt from the de.tbuchmann.bxagent.bags2bags Eclipse workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/de.tbuchmann.bxagent.bags2bags/bin && jar cf ${BUILD_TMP}/bxagent-bags2bags-1.0.0.jar de/
# install "org.benchmarx.tools" "bxagent-bags2bags"     "1.0.0"          "${BUILD_TMP}/bxagent-bags2bags-1.0.0.jar"
install "org.benchmarx.tools" "bx-runtime"            "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-bag12bag2 / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxBag1ToBag2/pom.xml depends on (supersedes the org.benchmarx.tools:* jars above).
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-bag12bag2" "1.0.0-SNAPSHOT" "${LIB_BAG}/de-tbuchmann-bxagent-bag12bag2-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "emt-agent"              "1.0.0-SNAPSHOT" "${LIB_BAG}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "plainjavaubt-bags12bags2" "1.0.0"        "${LIB_BAG}/PlainJavaUbtBags12Bags2.jar"
install "org.benchmarx.tools" "plainjavaubt-util"         "1.0.0"        "${LIB_BAG}/PlainJavaUbtUtil.jar"
install "org.benchmarx.tools" "mediniQVT"              "1.0.0"          "${LIB_BAG}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"                 "1.0.0"          "${LIB_BAG}/mediniQVT/qvtemf.jar"

# ── Set to OSet Tool JARs ─────────────────────────────────────────────────────
LIB_SET="${SCRIPT_DIR}/examples/settooset/BenchmarxSetToOSet/lib"

install "org.benchmarx.tools" "bxtend-set2oset"   "1.0.0"          "${LIB_SET}/bxtend-set2oset-1.0.0.jar"
# bxlang-set2oset: BXLang excluded for now (see per-example TestCase.java tools()).
#   CAUTION §2 – if re-enabled, rebuild from Set2OSets-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Set2OSets-bxlang/bin && jar cf ${BUILD_TMP}/bxlang-set2oset-1.0.0.jar dev/
# install "org.benchmarx.tools" "bxlang-set2oset"   "1.0.0"          "${BUILD_TMP}/bxlang-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-set2oset"  "1.0.0"          "${LIB_SET}/bxagent-set2oset-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_SET}/emt-agent-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "bx-runtime"         "1.0.0-SNAPSHOT" "${LIB_SET}/bx-runtime-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-set2oset / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxSetToOSet/pom.xml depends on (supersedes the org.benchmarx.tools:* jars above).
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-set2oset" "1.0.0-SNAPSHOT" "${LIB_SET}/de-tbuchmann-bxagent-set2oset-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                    "1.0.0-SNAPSHOT" "${LIB_SET}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "plainjavaubt-set2oset" "1.0.0"          "${LIB_SET}/PlainJavaUbtSet2Oset.jar"
install "org.benchmarx.tools" "plainjavaubt-util"     "1.0.0"          "${LIB_SET}/PlainJavaUbtUtil.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_SET}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_SET}/mediniQVT/qvtemf.jar"


# ── Gantt to CPM Tool JARs ────────────────────────────────────────────────────
LIB_G2C="${SCRIPT_DIR}/examples/gantttocpm/BenchmarxGanttToCPM/lib"

install "org.benchmarx.tools" "bxtend-gantt2cpm"  "1.0.0"          "${LIB_G2C}/bxtend-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-gantt2cpm" "1.0.0"          "${LIB_G2C}/bxagent-gantt2cpm-1.0.0.jar"
# bxlang-gantt2cpm: BXLang excluded for now (see per-example TestCase.java tools()).
#   CAUTION §2 – BXtend-Gantt2CPM.jar has wrong package; if re-enabled, rebuild from
#   Gantt2CPM-bxlang workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/Gantt2CPM-bxlang/bin && jar cf ${BUILD_TMP}/bxlang-gantt2cpm-1.0.0.jar dev/
# install "org.benchmarx.tools" "bxlang-gantt2cpm"  "1.0.0"          "${BUILD_TMP}/bxlang-gantt2cpm-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_G2C}/emt-agent-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-gantt2cpm / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxGanttToCPM/pom.xml depends on.
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-gantt2cpm" "1.0.0-SNAPSHOT" "${LIB_G2C}/de-tbuchmann-bxagent-gantt2cpm-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB_G2C}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_G2C}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_G2C}/mediniQVT/qvtemf.jar"

# -- Petrinet to PetrinetWeighted Tool JARs -----------------------------------
LIB_PN="${SCRIPT_DIR}/examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted/lib"

# bxtend-pn2pnw: lib/bxtend-pn2pnw-1.0.0.jar contains the correct package
#   de.tbuchmann.bxtend.pn2pnw.* (differs from the obsolete BXtend-PN2PNW.jar).
install "org.benchmarx.tools" "bxtend-pn2pnw"    "1.0.0"          "${LIB_PN}/bxtend-pn2pnw-1.0.0.jar"

# emt-agent-pn2pnw: obsolete, no pom.xml depends on it anymore (superseded by
# de-tbuchmann-bxagent-pn2pnw below); kept commented out since its jar must be
# manually rebuilt from the de.tbuchmann.bxagent.pn2pnw Eclipse workspace project:
#   cd /home/tb/workspaceBenchmarXUpdate/de.tbuchmann.bxagent.pn2pnw/bin
#   jar cf ${BUILD_TMP}/emt-agent-pn2pnw-1.0.0-SNAPSHOT.jar de/
# install "org.benchmarx.tools" "emt-agent-pn2pnw" "1.0.0-SNAPSHOT" "${BUILD_TMP}/emt-agent-pn2pnw-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "emt-agent"         "1.0.0-SNAPSHOT" "${LIB_PN}/emt-agent-1.0.0-SNAPSHOT.jar"
# de-tbuchmann-bxagent-pn2pnw / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxPetrinetToPetrinetWeighted/pom.xml depends on.
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-pn2pnw" "1.0.0-SNAPSHOT" "${LIB_PN}/de-tbuchmann-bxagent-pn2pnw-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                  "1.0.0-SNAPSHOT" "${LIB_PN}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_PN}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_PN}/mediniQVT/qvtemf.jar"

# ── Families to Persons Tool JARs ─────────────────────────────────────────────
LIB_F2P="${SCRIPT_DIR}/examples/familiestopersons/BenchmarxFamiliesToPersons/lib"

# bxtend-f2p: lib/bxtend-f2p-1.0.0.jar
# Contains de.tbuchmann.bxtend.f2p.rules.* (BXtendFamiliesToPersons)
install "org.benchmarx.tools" "bxtend-f2p"    "1.0.0"          "${LIB_F2P}/bxtend-f2p-1.0.0.jar"

# bxtenddsl-f2p: lib/BXtendDSLSynch.jar
# Contains de.tbuchmann.bxtenddsl.f2p.trafo.* (BXtendDSLFamiliesToPersons)
install "org.benchmarx.tools" "bxtenddsl-f2p" "1.0.0"          "${LIB_F2P}/BXtendDSLSynch.jar"

# emt-agent-f2p: obsolete, no pom.xml depends on it anymore (superseded by
# de-tbuchmann-bxagent-f2p below); its jar (lib/BXAgent.jar) has also moved to
# oldVersion/BXAgent.jar and is no longer installed.
# install "org.benchmarx.tools" "emt-agent-f2p" "1.0.0-SNAPSHOT" "${LIB_F2P}/BXAgent.jar"

# emt-agent: obsolete, no pom.xml depends on it anymore (superseded by dev.bxagent:bx-runtime
# below); its jar is also no longer present in lib/.
# install "org.benchmarx.tools" "emt-agent"     "1.0.0-SNAPSHOT" "${LIB_F2P}/emt-agent-1.0.0-SNAPSHOT.jar"

# bx-runtime: lib/bx-runtime-1.0.0-SNAPSHOT.jar
install "org.benchmarx.tools" "bx-runtime"    "1.0.0-SNAPSHOT" "${LIB_F2P}/bx-runtime-1.0.0-SNAPSHOT.jar"

# de-tbuchmann-bxagent-f2p / dev.bxagent:bx-runtime: the actual coordinates
# BenchmarxFamiliesToPersons/pom.xml depends on (supersedes emt-agent-f2p/emt-agent/bx-runtime above).
install "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-f2p" "1.0.0-SNAPSHOT" "${LIB_F2P}/de-tbuchmann-bxagent-f2p-1.0.0-SNAPSHOT.jar"
install "dev.bxagent"          "bx-runtime"                "1.0.0-SNAPSHOT" "${LIB_F2P}/bx-runtime-1.0.0-SNAPSHOT.jar"

# mediniQVT + qvtemf (shared coordinates with other examples, re-installing is idempotent)
install "org.benchmarx.tools" "mediniQVT"     "1.0.0"          "${LIB_F2P}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"        "1.0.0"          "${LIB_F2P}/mediniQVT/qvtemf.jar"


# ── Ecore to SQL Tool JARs ────────────────────────────────────────────────────
LIB_E2S="${SCRIPT_DIR}/examples/ecoretosql/BenchmarxEcoreToSQL/lib"

install "org.benchmarx.tools" "bxtend-ecore2sql"  "1.0.0"          "${LIB_E2S}/bxtend-ecore2sql-1.0.0.jar"
install "org.benchmarx.tools" "bxagent-ecore2sql" "1.0.0"          "${LIB_E2S}/bxagent-ecore2sql-1.0.0.jar"
install "org.benchmarx.tools" "emt-agent"          "1.0.0-SNAPSHOT" "${LIB_E2S}/emt-agent-1.0.0-SNAPSHOT.jar"
# New BXAgent transformation (supersedes bxagent-ecore2sql)
install "org.benchmarx.tools" "de-tbuchmann-bxagent-ecore2sql" "1.0.0-SNAPSHOT" "${LIB_E2S}/de-tbuchmann-bxagent-ecore2sql-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "bx-runtime"         "1.0.0-SNAPSHOT" "${LIB_E2S}/bx-runtime-1.0.0-SNAPSHOT.jar"
install "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_E2S}/mediniQVT/mediniQVT.jar"
install "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_E2S}/mediniQVT/qvtemf.jar"

echo ""
echo "All local deps installed. You can now run: ./mvnw test"
