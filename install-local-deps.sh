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

echo ""
echo "All local deps installed. You can now run: ./mvnw test"
