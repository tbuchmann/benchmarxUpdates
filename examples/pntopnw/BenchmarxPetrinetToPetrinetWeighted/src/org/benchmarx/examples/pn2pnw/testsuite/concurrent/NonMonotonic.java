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
 * Concurrent non-monotonic tests for PetriNet-to-PetriNetWeighted: source performs a
 * deletion while target independently performs an (unrelated) attribute change in the
 * same concurrent step, as opposed to {@link MonotonicCreating}/{@link MonotonicDeleting}
 * which only ever exercise one direction of change.
 * Weight is a target-only concept (no corresponding attribute on the unweighted source
 * side), so a concurrent target-side weight change here doesn't depend on backward
 * propagation working - matches the existing, passing
 * {@code MonotonicCreating.testConcurrentSourceIdleTargetChangeWeight} pattern.
 */
@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends Pn2PnwTestCase {

	public NonMonotonic() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for a non-monotonic concurrent step: source removes the C/2/D
	 * extension (deletion) while target independently changes the A-1-B edge weights
	 * from 4/2 to 7/3 (attribute change) (NM-DelSrcWeightTrg).<br/>
	 * <b>Expect</b>: Both models revert to the simple PTP structure, with the new 7/3
	 * weights preserved on the target.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, del+attribute
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceReduceTargetChangeWeight(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits, helperPn::extendPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		util.assertPrecondition("NonMonotonicPreExtendedPn", "NonMonotonicPreExtendedWeightedPnw");
		// Concurrent: SRC removes the C/2/D extension (delete); TRG independently
		// changes the A-1-B weights (attribute change) - a mixed, non-conflicting edit.
		tool.performAndPropagateEdit(
				srcEdit(helperPn::reducePTPExtendedLettersDigits),
				trgEdit(helperPnw::weightA1BWith73));
		util.assertPostcondition("NonMonotonicPostPn", "NonMonotonicPostWeightedPnw");
		terminate();
	}
}
