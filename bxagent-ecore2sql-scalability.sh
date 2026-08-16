#!/bin/bash
./mvnw -pl examples/ecoretosql/BenchmarxEcoreToSQL -am test -Dbenchmarx.tool=BXAgentEcore2SQL -Dtest='ScalabilityBatchTestsFwd,ScalabilityBatchTestsBwd,ScalabilityIncrTestsFwd,ScalabilityIncrTestsBwd,ScalabilityConstDeltaCSync,ScalabilityConstDeltaCFCSync,ScalabilityConstModelCSync,ScalabilityConstModelCFCSync'   -Dsurefire.failIfNoSpecifiedTests=false
