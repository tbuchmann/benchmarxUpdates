#!/usr/bin/env bash
# Scalability sweep for the BXAgent tool across all examples.
# See scalability-lib.sh for the timeout/kill behaviour and env overrides.
source "$(dirname "${BASH_SOURCE[0]}")/scalability-lib.sh"

run_sweep bxagent \
  examples/asttodag/BenchmarxAstToDag                    BXAgentAst2Dag \
  examples/bag1tobag2/BenchmarxBag1ToBag2                BXAgentBags2Bags \
  examples/ecoretosql/BenchmarxEcoreToSQL                BXAgentEcore2SQL \
  examples/familiestopersons/BenchmarxFamiliesToPersons  BXAgentF2p \
  examples/gantttocpm/BenchmarxGanttToCPM                BXAgentGantt2Cpm \
  examples/pdb1topdb2/BenchmarxPdb1ToPdb2                BXAgentPdb12Pdb2 \
  examples/pntopnw/BenchmarxPetrinetToPetrinetWeighted   BXAgentPn2Pnw \
  examples/settooset/BenchmarxSetToOSet                  BXAgentSet2OSet
