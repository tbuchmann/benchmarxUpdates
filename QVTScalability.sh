#!/usr/bin/env bash
# Scalability sweep for the MediniQVT tool across all examples.
# See scalability-lib.sh for the timeout/kill behaviour and env overrides.
#
# MediniQVT is the tool that tends to leave non-cooperative threads running
# past the in-JUnit timeout, so the per-class OS-level `timeout` + SIGKILL in
# scalability-lib.sh matters most here. Tighten the ceiling if you want, e.g.:
#   SCALABILITY_CEILING=300 ./QVTScalability.sh
source "$(dirname "${BASH_SOURCE[0]}")/scalability-lib.sh"

run_sweep mediniqvt \
  examples/asttodag/BenchmarxAstToDag                    MediniQVTAst2Dag \
  examples/bag1tobag2/BenchmarxBag1ToBag2                MediniQVTBag12Bag2 \
  examples/ecoretosql/BenchmarxEcoreToSQL                MediniQVTEcore2SQL \
  examples/familiestopersons/BenchmarxFamiliesToPersons  MediniQVTFamiliesToPersons \
  examples/gantttocpm/BenchmarxGanttToCPM                MediniQVTGantt2CPM \
  examples/pdb1topdb2/BenchmarxPdb1ToPdb2                MediniQVTPdb12Pdb2 \
  examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted   MediniQVTPn2Pnw \
  examples/settooset/BenchmarxSetToOSet                  MediniQVTSetToOSet
