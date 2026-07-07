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
 * Concurrent monotonic-deletion tests for PetriNet-to-PetriNetWeighted.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicDeleting extends Pn2PnwTestCase {

	public MonotonicDeleting() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source reduction while target is idle (MD-FwdReduce).<br/>
	 * Starting from the extended PTP net with custom weights, the source removes C/2/D
	 * concurrently while the target performs no edit.<br/>
	 * <b>Expect</b>: Both models revert to the simple PTP state; existing weights are preserved.<br/>
	 * <b>Features</b>: concurrent, del, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceReduceTargetIdle(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits, helperPn::extendPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		// Concurrent: SRC removes C/2/D; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperPn::reducePTPExtendedLettersDigits),
				trgEdit(helperPnw::idleDelta));
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		terminate();
	}
}
