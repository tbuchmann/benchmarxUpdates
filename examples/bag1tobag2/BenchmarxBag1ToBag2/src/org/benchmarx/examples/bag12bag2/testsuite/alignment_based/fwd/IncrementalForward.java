package org.benchmarx.examples.bag12bag2.testsuite.alignment_based.fwd;

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
public class IncrementalForward extends Bag12Bag2TestCase {

	public IncrementalForward() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting the elements into an existing bag1. Elements with same and different values will be added to bag1.<br/>
	 * <b>Expect</b> : New elements are added to the amount of the existing elements in bag2 and one new Element should be created.<br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createOneBeer));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));
		
		util.assertPrecondition("OneBeerBags1", "OneBeerIncrIDBags2");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass,
				helperBag1::createBeerGlass));
		//------------
		util.assertPostcondition("SixBeerWithTwoGlassesBags1", "SixBeerWithTwoGlassesBags2");
		terminate();
	}
	
	/**
	 * <b>Test</b> for deleting elements. 2 Elements with value Beer and 1 with value Beer Glass are deleted from a bag1 containing 5 Beers and 1 Beer Glass.
	 * <b>Expect</b>: Change of multiplicity in bag2 of Element with value Beer and Deletion of Element with value Beer Glass.
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));
		
		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassIncrIDBags2");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::deleteBeer,
				helperBag1::deleteBeer,
				helperBag1::deleteBeerGlass));
		//------------
		util.assertPostcondition("ThreeBeerBags1", "ThreeBeerBags2");
		terminate();
	}
	
	/**
	 * <b>Test</b> for changing the value of Beer in a single element of bag1 to EmptyBottle.
	 * <b>Expect</b>: Change occurs also in bag2.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalValueChangeOfOne(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));
		
		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassIncrIDBags2");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::changeOneBeerToEmptyBottle));
		tool.performIdleSourceEdit(srcEdit(helperBag1::changeIncrementalID));
		//------------
		util.assertPostcondition("FourBeerOneEmptyBottleWithGlassBags1", "FourBeerOneEmptyBottleWithGlassIncrIDBags2");
		terminate();
	}
	
	/**
	 * <b>Test</b> for changing the value of Beer of all occurences of Beer in bag1 to Empty Bottle.
	 * <b>Expect</b>: Change occurs also in bag2: A Element with multiplicity 5 and value Empty Bottle should be there.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalValueChangeOfAll(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));
		
		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassIncrIDBags2");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::changeAllBeerToEmptyBottles));
		//------------
		util.assertPostcondition("FiveEmptyBottlesWithGlassBags1", "FiveEmptyBottlesWithGlassBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass,
				helperBag1::createBeerGlass,
				helperBag1::createOneBeer));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));

		util.assertPrecondition("SixBeerWithTwoGlassesBags1", "SixBeerWithTwoGlassesBags2");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperBag1::idleDelta));
		//------------
		util.assertPostcondition("SixBeerWithTwoGlassesBags1", "SixBeerWithTwoGlassesBags2");
		terminate();
	}
}