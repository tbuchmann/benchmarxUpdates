package org.benchmarx.examples.pn2pnw.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent monotonic-creation tests for PetriNet-to-PetriNetWeighted.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends Pn2PnwTestCase {

	public MonotonicCreating() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source extension while target is idle (MC-FwdExtend).<br/>
	 * Starting from a PTP net with custom weights, the source adds C/2/D concurrently
	 * while the target performs no edit.<br/>
	 * <b>Expect</b>: Extended PTP net on both sides, with the pre-existing weights preserved.<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceExtendTargetIdle(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		// Concurrent: SRC extends with C/2/D; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperPn::extendPTPLettersDigits),
				trgEdit(helperPnw::idleDelta));
		util.assertPostcondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent weight change on target while source is idle (MC-BwdWeight).<br/>
	 * Starting from a PTP net with weights 4/2, the target changes them to 7/3 concurrently
	 * while the source performs no edit.<br/>
	 * <b>Expect</b>: Source unchanged; target reflects the new 7/3 weights.<br/>
	 * <b>Features</b>: concurrent, attribute, bwd-dominant, target-only
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceIdleTargetChangeWeight(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		// Concurrent: SRC idle; TRG changes edge weights from 4/2 to 7/3
		tool.performAndPropagateEdit(
				srcEdit(helperPn::idleDelta),
				trgEdit(helperPnw::weightA1BWith73));
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeighted73Pnw");
		terminate();
	}
}
