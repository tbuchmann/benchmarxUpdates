package org.benchmarx.examples.bag12bag2.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.Bag12Bag2TestCase;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import bags1.MyBag;

@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends Bag12Bag2TestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for a round-trip involving additions on both sides.<br/>
	 * Starting from a bag containing one Beer, a Beer Glass is added to bag1 (forward),
	 * then an Empty Bottle is added to bag2 (backward).<br/>
	 * <b>Expect</b>: Both models contain Beer, Beer Glass and Empty Bottle.<br/>
	 * <b>Features</b>: roundtrip, add, fwd+bwd
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripAdd(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createOneBeer));

		util.assertPrecondition("OneBeerBags1", "OneBeerBags2");
		// ── forward: add Beer Glass to source ──
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createBeerGlass));
		// intermediate: One Beer + One Beer Glass
		util.assertPostcondition("OneBeerWithGlassBags1", "OneBeerWithGlassBags2");
		// ── backward: add Empty Bottle to target ──
		tool.performAndPropagateTargetEdit(trgEdit(
				helperBag2::createEmptyBottle));
		// ── final postcondition ──
		util.assertPostcondition("OneBeerOneEmptyBottleWithGlassBags1", "OneBeerOneEmptyBottleWithGlassBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for a round-trip involving deletions on both sides.<br/>
	 * Starting from a bag with five Beers and one Beer Glass, one Beer is deleted from bag1
	 * (forward), then the Beer Glass is deleted from bag2 (backward).<br/>
	 * <b>Expect</b>: Both models contain exactly four Beers and no Beer Glass.<br/>
	 * <b>Features</b>: roundtrip, del, fwd+bwd, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripDelete(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── forward: delete one Beer from source ──
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::deleteBeer));
		// intermediate: four Beers + Beer Glass
		util.assertPostcondition("FourBeerWithGlassBags1", "FourBeerWithGlassBags2");
		// ── backward: delete Beer Glass from target ──
		tool.performAndPropagateTargetEdit(trgEdit(
				helperBag2::deleteBeerGlass));
		// ── final postcondition ──
		util.assertPostcondition("FourBeerBags1", "FourBeerBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for a round-trip involving attribute changes (value edits) on both sides.<br/>
	 * Starting from a bag with five Beers and one Beer Glass, one Beer is changed to
	 * Empty Bottle in bag1 (forward), then the Empty Bottle is changed to Broken Bottle
	 * in bag2 (backward).<br/>
	 * <b>Expect</b>: Both models contain four Beers, one Broken Bottle and one Beer Glass.<br/>
	 * <b>Features</b>: roundtrip, attribute, fwd+bwd, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripEdit(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── forward: change one Beer to Empty Bottle in source ──
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::changeOneBeerToEmptyBottle));
		// intermediate: four Beers + Empty Bottle + Beer Glass
		util.assertPostcondition("FourBeerOneEmptyBottleWithGlassBags1woInc", "FourBeerOneEmptyBottleWithGlassBags2");
		// ── backward: change Empty Bottle to Broken Bottle in target ──
		tool.performAndPropagateTargetEdit(trgEdit(
				helperBag2::changeEmptyBottleToBrokenBottle));
		// ── final postcondition ──
		util.assertPostcondition("FourBeerOneBrokenBottleWithGlassBags1", "FourBeerOneBrokenBottleWithGlassBags2");
		terminate();
	}
}