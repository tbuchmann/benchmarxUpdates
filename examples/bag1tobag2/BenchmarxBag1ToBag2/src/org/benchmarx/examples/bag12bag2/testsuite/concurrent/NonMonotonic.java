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
 * Test class for non-monotonic concurrent edits on both sides of the
 * Bag1-to-Bag2 transformation. "Non-monotonic" means that at least one side
 * performs a mix of creation and deletion (or a deletion paired with an
 * attribute change) in the same concurrent step.
 */
@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends Bag12Bag2TestCase {

	public NonMonotonic() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent delete-on-source and create-on-target
	 * (NM-CreateDelete).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, the Beer Glass is
	 * deleted from bag1 while an Empty Bottle is created in bag2 concurrently.<br/>
	 * <b>Expect</b>: The Beer Glass deletion and the Empty Bottle creation are both
	 * reflected: both models contain five Beers and one Empty Bottle.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, del+add, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentDeleteSourceAddTarget(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent edit: SRC deletes Beer Glass, TRG adds Empty Bottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeerGlass),
				trgEdit(helperBag2::createEmptyBottle));
		// ── postcondition: five Beers + one Empty Bottle ──
		util.assertPostcondition("FiveBeerOneEmptyBottleBags1", "FiveBeerOneEmptyBottleBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent create-on-source and delete-on-target
	 * (NM-CreateDelete reverse).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, a new Beer Glass is added
	 * to bag1 while the Beer Glass element is deleted from bag2 concurrently.<br/>
	 * <b>Expect</b>: The net effect is that no Beer Glass remains in either model because
	 * the source creation is overridden by the target deletion, or the tool merges both
	 * changes resulting in five Beers only. The source addition cannot survive when
	 * the target explicitly deletes the corresponding element.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, add+del, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentAddSourceDeleteTarget(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent edit: SRC adds another Beer Glass, TRG deletes Beer Glass ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::createBeerGlass),
				trgEdit(helperBag2::deleteBeerGlass));
		// ── postcondition: five Beers + one Beer Glass (SRC add survives the TRG delete) ──
		util.assertPostcondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent delete-on-source and attribute-change-on-target
	 * of a different element (NM-RenameDelete).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, one Beer is deleted from
	 * bag1 while the Beer Glass element's value is changed to Empty Bottle in bag2
	 * concurrently.<br/>
	 * <b>Expect</b>: Both changes are reflected: both models contain four Beers and
	 * one Empty Bottle.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, del+attribute, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentDeleteSourceRenameTarget(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent edit: SRC deletes one Beer, TRG renames Beer Glass → Empty Bottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeer),
				trgEdit(helperBag2::changeBeerGlassToEmptyBottle));
		// ── postcondition: four Beers + one Empty Bottle ──
		util.assertPostcondition("FourBeerOneEmptyBottleBags1", "FourBeerOneEmptyBottleBags2");
		terminate();
	}
}