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
 * Test class for concurrent creation on both sides of the Bag1-to-Bag2
 * transformation. Both models are edited independently (without synchronization)
 * and synchronization is triggered afterwards via
 * {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends Bag12Bag2TestCase {

	public MonotonicCreating() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent addition of elements with different values
	 * on both sides (MC-NonMatching).<br/>
	 * Starting from a bag with one Beer, a Beer Glass is added to bag1 while an
	 * Empty Bottle is added to bag2 concurrently.<br/>
	 * <b>Expect</b>: Both additions are reflected in both models: Beer, Beer Glass,
	 * and Empty Bottle present on each side.<br/>
	 * <b>Features</b>: concurrent, add, non-matching
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentAddNonMatchingValues(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createOneBeer));

		util.assertPrecondition("OneBeerBags1", "OneBeerBags2");
		// ── concurrent edit: SRC adds Beer Glass, TRG adds Empty Bottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::createBeerGlass),
				trgEdit(helperBag2::createEmptyBottle));
		// ── postcondition ──
		util.assertPostcondition("MCNonMatchingValuesBags1", "MCNonMatchingValuesBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent addition of elements with the same value
	 * on both sides (MC-Matching).<br/>
	 * Starting from a bag with one Beer, a Beer Glass is added to bag1 while a
	 * Beer Glass is also added to bag2 concurrently.<br/>
	 * <b>Expect</b>: Both Beer Glass additions are reflected: bag1 has Beer + 2×Beer Glass,
	 * bag2 has Beer:1 + Beer Glass:2.<br/>
	 * <b>Features</b>: concurrent, add, matching-type
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentAddSameValue(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createOneBeer));

		util.assertPrecondition("OneBeerBags1", "OneBeerBags2");
		// ── concurrent edit: both sides add Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::createBeerGlass),
				trgEdit(helperBag2::createBeerGlass));
		// ── postcondition ── only one beer glass created on each side, but both have the same value (Beer Glass:1) ──
		util.assertPostcondition("MCSameValueBags1", "MCSameValueBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent addition of multiple elements with different values
	 * from an empty starting state (MC-Combined).<br/>
	 * Five Beers and a Beer Glass are added to bag1 while an Empty Bottle and a
	 * Beer Glass are added to bag2 concurrently.<br/>
	 * <b>Expect</b>: All additions are reflected: bag1 has 5 Beers, 1 Empty Bottle, 2 Beer Glasses;
	 * bag2 has Beer:5, Empty Bottle:1, Beer Glass:2.<br/>
	 * <b>Features</b>: concurrent, add, combined
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentAddCombined(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();

		util.assertPrecondition("RootElementBags1", "RootElementBags2");
		// ── concurrent edit: SRC adds 5 Beers + Beer Glass, TRG adds Empty Bottle + Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::createFiveBeers, helperBag1::createBeerGlass),
				trgEdit(helperBag2::createEmptyBottle, helperBag2::createBeerGlass));
		// ── postcondition : beer glass was simultaneously created on both sides, establish match ──
		util.assertPostcondition("MCCombinedBags1", "MCCombinedBags2");
		terminate();
	}
}