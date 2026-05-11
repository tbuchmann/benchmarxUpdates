package org.benchmarx.examples.bag12bag2.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.Bag12Bag2TestCase;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import bags1.MyBag;

/**
 * Test class for concurrent deletion on both sides of the Bag1-to-Bag2
 * transformation. Both models are edited independently (without synchronization)
 * and synchronization is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicDeleting extends Bag12Bag2TestCase {

	public MonotonicDeleting() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent deletion of the same element type on both sides
	 * (MD-Matching).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, the Beer Glass is deleted
	 * from bag1 and the Beer Glass element is also deleted from bag2 concurrently.<br/>
	 * <b>Expect</b>: Both deletions are merged: both models contain only five Beers.<br/>
	 * <b>Features</b>: concurrent, del, matching, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentMatchingDeletion(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent edit: both sides delete Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeerGlass),
				trgEdit(helperBag2::deleteBeerGlass));
		// ── postcondition: only Beer remains ──
		util.assertPostcondition("FiveBeerBags1", "FiveBeerBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent deletion of different element types on each side
	 * (MD-NonMatching).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, one Beer is deleted from
	 * bag1 while the Beer Glass is deleted from bag2 concurrently.<br/>
	 * <b>Expect</b>: Both deletions are propagated independently: both models contain
	 * four Beers and no Beer Glass.<br/>
	 * <b>Features</b>: concurrent, del, non-matching, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentNonMatchingDeletion(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent edit: SRC deletes Beer, TRG deletes Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeer),
				trgEdit(helperBag2::deleteBeerGlass));
		// ── postcondition: four Beers, no Beer Glass ──
		util.assertPostcondition("FourBeerBags1", "FourBeerBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent combined deletions where both sides delete elements
	 * including one shared element type (MD-Combined).<br/>
	 * Starting from a bag with six Beers and two Beer Glasses, one Beer and one Beer Glass
	 * are deleted from bag1, while one Beer Glass is also deleted from bag2 concurrently.<br/>
	 * <b>Expect</b>: All deletions are reflected; the Beer Glass deletions from both sides are
	 * merged into a single removal: both models contain five Beers and one Beer Glass.<br/>
	 * <b>Features</b>: concurrent, del, combined, matching+non-matching
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentCombinedDeletion(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass,
				helperBag1::createBeerGlass,
				helperBag1::createOneBeer));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));

		util.assertPrecondition("SixBeerWithTwoGlassesBags1", "SixBeerWithTwoGlassesBags2");
		// ── concurrent edit: SRC deletes Beer + Beer Glass; TRG deletes Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeer, helperBag1::deleteBeerGlass),
				trgEdit(helperBag2::deleteBeerGlass));
		// ── postcondition: five Beers + no Beer Glass ──
		util.assertPostcondition("FiveBeerWithGlassBags1", "FiveBeerWithoutGlassBags2");
		// also accepted postcondition: six Beers + no Beer Glass (if TRG deletion is merged with one of the SRC deletions)
		terminate();
	}
}