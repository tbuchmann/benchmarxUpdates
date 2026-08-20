#!/bin/sh
# vendor-deps.sh
#
# Maintainer script: populates the committed repo/ Maven repository from the
# tool JARs already checked into each example's lib/ folder. A normal
# `./mvnw test` does NOT need this script — repo/ is committed, so a fresh
# clone resolves everything out of the box. Re-run this only when adding a
# new non-Central JAR (add a matching `vendor` line below, then run this).
#
# What it does, per JAR: installs it into repo/ under the given Maven
# coordinates (generates a pom + checksums), then replaces the copied jar
# file with a relative symlink back to the original file in lib/. This keeps
# the jar bytes stored exactly once (so Eclipse .classpath entries, which
# reference lib/ directly, keep working and repo/ doesn't double the ~236MB
# of vendored jars) while still giving Maven a normal file:// repository to
# resolve from.
#
# Some coordinates are shared across examples (mediniQVT, qvtemf, bx-runtime)
# because the underlying jars are byte-identical (or, for bx-runtime,
# identical after re-packaging) copies vendored into each example's own
# lib/ for Eclipse's benefit; whichever example's `vendor` line for that
# coordinate runs last "wins" as the symlink target, matching Maven's normal
# one-artifact-per-coordinate model.
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN="${SCRIPT_DIR}/mvnw"
REPO="${SCRIPT_DIR}/repo"

vendor() {
  GROUP="$1"; ARTIFACT="$2"; VERSION="$3"; JAR="$4"
  if [ ! -f "${JAR}" ]; then
    echo "SKIP (jar not found): ${GROUP}:${ARTIFACT}:${VERSION} -> ${JAR}" >&2
    return
  fi
  echo "Vendoring ${GROUP}:${ARTIFACT}:${VERSION}"
  "${MVN}" install:install-file \
    -Dfile="${JAR}" \
    -DgroupId="${GROUP}" \
    -DartifactId="${ARTIFACT}" \
    -Dversion="${VERSION}" \
    -Dpackaging=jar \
    -DgeneratePom=true \
    -DcreateChecksum=true \
    -DlocalRepositoryPath="${REPO}" \
    -q

  GPATH=$(echo "${GROUP}" | tr '.' '/')
  TARGET="${REPO}/${GPATH}/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar"
  rm -f "${TARGET}"
  REL=$(python3 -c "import os,sys; print(os.path.relpath(sys.argv[1], sys.argv[2]))" "${JAR}" "$(dirname "${TARGET}")")
  ln -s "${REL}" "${TARGET}"
}

# ── emf.compare (not on Maven Central) ───────────────────────────────────────
vendor "org.eclipse.emf.compare" "org.eclipse.emf.compare" "3.5.3" "${SCRIPT_DIR}/core/Benchmarx/lib/org.eclipse.emf.compare-3.5.3.202508281322.jar"

# ── PDB1 to PDB2 Tool JARs ────────────────────────────────────────────────────
LIB="${SCRIPT_DIR}/examples/pdb1topdb2/BenchmarxPdb1ToPdb2/lib"
vendor "org.benchmarx.tools" "bxtend-pdb12pdb2"  "1.1.0"          "${LIB}/bxtend-pdb12pdb2-1.1.0.jar"
vendor "org.benchmarx.tools" "bxagent-pdb12pdb2" "1.0.0"          "${LIB}/bxagent-pdb12pdb2-1.0.0.jar"
vendor "org.benchmarx.tools" "bx-runtime"        "1.0.0-SNAPSHOT" "${LIB}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-pdb12pdb2" "1.0.0-SNAPSHOT" "${LIB}/de-tbuchmann-bxagent-pdb12pdb2-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB}/mediniQVT/qvtemf.jar"

# ── AST to DAG Tool JARs ──────────────────────────────────────────────────────
LIB_AST="${SCRIPT_DIR}/examples/asttodag/BenchmarxAstToDag/lib"
vendor "org.benchmarx.tools" "bxtend-ast2dag"  "1.2.0"          "${LIB_AST}/bxtend-ast2dag-1.2.0.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-ast2dag" "1.0.0-SNAPSHOT" "${LIB_AST}/de-tbuchmann-bxagent-ast2dag-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                   "1.0.0-SNAPSHOT" "${LIB_AST}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"        "1.0.0"          "${LIB_AST}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"           "1.0.0"          "${LIB_AST}/mediniQVT/qvtemf.jar"

# ── Bag1 to Bag2 Tool JARs ────────────────────────────────────────────────────
LIB_BAG="${SCRIPT_DIR}/examples/bag1tobag2/BenchmarxBag1ToBag2/lib"
vendor "org.benchmarx.tools" "bxtend-bag12bag2"      "1.2.0"          "${LIB_BAG}/bxtend-bag12bag2-1.2.0.jar"
vendor "org.benchmarx.tools" "bx-runtime"            "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-bag12bag2" "1.0.0-SNAPSHOT" "${LIB_BAG}/de-tbuchmann-bxagent-bag12bag2-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB_BAG}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"              "1.0.0"          "${LIB_BAG}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"                 "1.0.0"          "${LIB_BAG}/mediniQVT/qvtemf.jar"

# ── Set to OSet Tool JARs ─────────────────────────────────────────────────────
LIB_SET="${SCRIPT_DIR}/examples/settooset/BenchmarxSetToOSet/lib"
vendor "org.benchmarx.tools" "bxtend-set2oset"   "1.1.0"          "${LIB_SET}/bxtend-set2oset-1.1.0.jar"
vendor "org.benchmarx.tools" "bxagent-set2oset"  "1.0.0"          "${LIB_SET}/bxagent-set2oset-1.0.0.jar"
vendor "org.benchmarx.tools" "bx-runtime"         "1.0.0-SNAPSHOT" "${LIB_SET}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-set2oset" "1.0.0-SNAPSHOT" "${LIB_SET}/de-tbuchmann-bxagent-set2oset-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                    "1.0.0-SNAPSHOT" "${LIB_SET}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_SET}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_SET}/mediniQVT/qvtemf.jar"

# ── Gantt to CPM Tool JARs ────────────────────────────────────────────────────
LIB_G2C="${SCRIPT_DIR}/examples/gantttocpm/BenchmarxGanttToCPM/lib"
vendor "org.benchmarx.tools" "bxtend-gantt2cpm"  "1.1.0"          "${LIB_G2C}/bxtend-gantt2cpm-1.1.0.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-gantt2cpm" "1.0.0-SNAPSHOT" "${LIB_G2C}/de-tbuchmann-bxagent-gantt2cpm-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                     "1.0.0-SNAPSHOT" "${LIB_G2C}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_G2C}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_G2C}/mediniQVT/qvtemf.jar"

# ── Petrinet to PetrinetWeighted Tool JARs ────────────────────────────────────
LIB_PN="${SCRIPT_DIR}/examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted/lib"
vendor "org.benchmarx.tools" "bxtend-pn2pnw"    "1.1.0"          "${LIB_PN}/bxtend-pn2pnw-1.1.0.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-pn2pnw" "1.0.0-SNAPSHOT" "${LIB_PN}/de-tbuchmann-bxagent-pn2pnw-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                  "1.0.0-SNAPSHOT" "${LIB_PN}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_PN}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_PN}/mediniQVT/qvtemf.jar"

# ── Families to Persons Tool JARs ─────────────────────────────────────────────
LIB_F2P="${SCRIPT_DIR}/examples/familiestopersons/BenchmarxFamiliesToPersons/lib"
vendor "org.benchmarx.tools" "bxtend-f2p"    "1.0.0"          "${LIB_F2P}/bxtend-f2p-1.0.0.jar"
vendor "org.benchmarx.tools" "bxtenddsl-f2p" "1.0.0"          "${LIB_F2P}/BXtendDSLSynch.jar"
vendor "org.benchmarx.tools" "bx-runtime"    "1.0.0-SNAPSHOT" "${LIB_F2P}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-f2p" "1.0.0-SNAPSHOT" "${LIB_F2P}/de-tbuchmann-bxagent-f2p-1.0.0-SNAPSHOT.jar"
vendor "dev.bxagent"          "bx-runtime"                "1.0.0-SNAPSHOT" "${LIB_F2P}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"     "1.0.0"          "${LIB_F2P}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"        "1.0.0"          "${LIB_F2P}/mediniQVT/qvtemf.jar"

# ── Ecore to SQL Tool JARs ────────────────────────────────────────────────────
LIB_E2S="${SCRIPT_DIR}/examples/ecoretosql/BenchmarxEcoreToSQL/lib"
vendor "org.benchmarx.tools" "bxtend-ecore2sql"  "2.0.0"          "${LIB_E2S}/bxtend-ecore2sql-2.0.0.jar"
vendor "org.benchmarx.tools" "bxagent-ecore2sql" "1.0.0"          "${LIB_E2S}/bxagent-ecore2sql-1.0.0.jar"
vendor "de.tbuchmann.bxagent" "de-tbuchmann-bxagent-ecore2sql" "1.0.0-SNAPSHOT" "${LIB_E2S}/de-tbuchmann-bxagent-ecore2sql-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "bx-runtime"         "1.0.0-SNAPSHOT" "${LIB_E2S}/bx-runtime-1.0.0-SNAPSHOT.jar"
vendor "org.benchmarx.tools" "mediniQVT"          "1.0.0"          "${LIB_E2S}/mediniQVT/mediniQVT.jar"
vendor "org.benchmarx.tools" "qvtemf"             "1.0.0"          "${LIB_E2S}/mediniQVT/qvtemf.jar"

echo ""
echo "repo/ populated. Nothing further to run — ./mvnw test works directly."
