package org.benchmarx.examples.pn2pnw.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Round-trip tests for the PetriNet-to-PetriNetWeighted transformation.
 * Each test performs a sequence of forward and backward propagation steps
 * and verifies that the model states are consistent at each intermediate point.
 */
@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends Pn2PnwTestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for a round-trip: forward create PTP, then backward set weights, then forward extend.<br/>
	 * Weights are target-only; they must survive the round-trip unchanged on the PNW side.<br/>
	 * <b>Expect</b>: After backward weight-setting the source is unchanged. After forward extend
	 * both models reflect the extended PTP structure with the previously set weights.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, target-only-attribute
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripWeightChangeThenExtend(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		// Backward: set weights — source (PN) must remain unchanged
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		// Forward: extend the source net with C, 2, D
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::extendPTPLettersDigits));
		util.assertPostcondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for a round-trip: forward create+extend, backward idle, then forward reduce.<br/>
	 * <b>Expect</b>: Backward idle leaves both models unchanged. Forward reduce removes C/2/D
	 * from both models while preserving the target-side weights on the remaining edges.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, del
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripExtendThenReduce(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits, helperPn::extendPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		// Backward idle: neither model must change
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::idleDelta));
		util.assertPostcondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		// Forward reduce: remove the extended elements
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::reducePTPExtendedLettersDigits));
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for round-trip stability: forward idle then backward idle must not change either model.<br/>
	 * <b>Expect</b>: Both models remain in the precondition state after consecutive idle propagations.<br/>
	 * <b>Features</b>: roundtrip, stability, fwd+bwd
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripStability(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		// Forward idle: nothing changes
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::idleDelta));
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		// Backward idle: nothing changes
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::idleDelta));
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		terminate();
	}
}
