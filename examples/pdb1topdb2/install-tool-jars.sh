#!/bin/sh
# install-tool-jars.sh
# Registers all BenchmarxPdb1ToPdb2 tool JARs into the local Maven repository
# so that Tycho (pomDependencies=consider) can resolve them at build time.
#
# Usage:  sh install-tool-jars.sh
# Run once per machine, or whenever a JAR in lib/ changes.

set -e

BASE="$(cd "$(dirname "$0")" && pwd)"
LIB="$BASE/BenchmarxPdb1ToPdb2/lib"
GROUP="org.benchmarx.tools"
MVN="mvn"

install() {
    ARTIFACT="$1"
    VERSION="$2"
    JAR="$3"
    echo "Installing $ARTIFACT:$VERSION from $JAR"
    "$MVN" install:install-file \
        -Dfile="$JAR" \
        -DgroupId="$GROUP" \
        -DartifactId="$ARTIFACT" \
        -Dversion="$VERSION" \
        -Dpackaging=jar \
        -DgeneratePom=true \
        -DlocalRepositoryPath="$BASE/mvn-local-repo" \
        -DcreateChecksum=true
}

# ── Tool JARs ────────────────────────────────────────────────────────────────
install "bxtend-pdb12pdb2"      "1.0.0"          "$LIB/bxtend-pdb12pdb2-1.0.0.jar"
install "bxagent-pdb12pdb2"     "1.0.0"          "$LIB/bxagent-pdb12pdb2-1.0.0.jar"
install "bxlang-pdb12pdb2"      "1.0.0-SNAPSHOT" "$LIB/bxlang-pdb12pdb2.jar"
install "bx-runtime"            "1.0.0-SNAPSHOT" "$LIB/bx-runtime-1.0.0-SNAPSHOT.jar"
install "IBeXTGGPDB1ToPDB2"     "1.0.0"          "$LIB/IBeXTGGPDB1ToPDB2.jar"
install "PlainJavaUbtPdb12Pdb2" "1.0.0"          "$LIB/PlainJavaUbtPdb12Pdb2.jar"
install "PlainJavaUbtUtil"      "1.0.0"          "$LIB/PlainJavaUbtUtil.jar"
install "mediniQVT"             "1.0.0"          "$LIB/mediniQVT/mediniQVT.jar"
install "qvtemf"                "1.0.0"          "$LIB/mediniQVT/qvtemf.jar"

echo ""
echo "All tool JARs installed into $BASE/mvn-local-repo"
echo "To use them, ensure the following repository is declared in the root pom.xml:"
echo ""
echo "  <repository>"
echo "    <id>benchmarx-local-tools</id>"
echo "    <url>file://\${project.basedir}/mvn-local-repo</url>"
echo "  </repository>"
