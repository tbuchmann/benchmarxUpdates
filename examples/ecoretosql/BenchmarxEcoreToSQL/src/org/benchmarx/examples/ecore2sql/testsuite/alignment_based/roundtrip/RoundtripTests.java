package org.benchmarx.examples.ecore2sql.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

/**
 * Round-trip tests for the Ecore-to-SQL transformation.
 * Each test performs a sequence of forward and backward propagation steps and
 * verifies consistency at each intermediate point.
 */
@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends EcoreToSQLTestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}

	/**
	 * <b>Test</b> for a round-trip: forward create, then backward rename, then backward idle.<br/>
	 * Starting with the simple CompositeList Ecore/SQL state, the backward rename of the
	 * schema is propagated, then a backward idle confirms the new state is stable.<br/>
	 * <b>Expect</b>: After renameSchema both models reflect the renamed schema/package.
	 * Backward idle leaves the state unchanged.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripCreateThenRename(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName));
		util.assertPrecondition("RoundtripSimpleEcore", "RoundtripSimpleSQL");
		// Backward: rename the schema
		tool.performAndPropagateTargetEdit(trgEdit(helperSQL::renameSchema));
		util.assertPostcondition("RoundtripRenamedEcore", "RoundtripRenamedSQL");
		// Backward idle: state must remain stable
		tool.performAndPropagateTargetEdit(trgEdit(helperSQL::idleDelta));
		util.assertPostcondition("RoundtripRenamedEcore", "RoundtripRenamedSQL");
		terminate();
	}
}
