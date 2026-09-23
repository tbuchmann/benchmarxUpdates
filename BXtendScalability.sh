#!/usr/bin/env bash
# Scalability sweep for the BXtend tool across all examples.
# See scalability-lib.sh for the timeout/kill behaviour and env overrides.
#
# All BXtend adapters implement performAndPropagateEdit (via <transformation>.synch()),
# so the four *CSync / *CFCSync concurrent classes run for every example.
source "$(dirname "${BASH_SOURCE[0]}")/scalability-lib.sh"

run_sweep bxtend \
  examples/asttodag/BenchmarxAstToDag                    BXtendAst2Dag \
  examples/bag1tobag2/BenchmarxBag1ToBag2                BXtendBag12Bag2 \
  examples/ecoretosql/BenchmarxEcoreToSQL                BXtendEcore2SQL \
  examples/familiestopersons/BenchmarxFamiliesToPersons  BXtendFamiliesToPersons \
  examples/gantttocpm/BenchmarxGanttToCPM                BXtendGantt2CPM \
  examples/pdb1topdb2/BenchmarxPdb1ToPdb2                BXtendPdb12Pdb2 \
  examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted   BXtendPn2Pnw \
  examples/settooset/BenchmarxSetToOSet                  BXtendSet2Oset
