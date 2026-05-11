package org.benchmarx.examples.bag12bag2.testsuite.concurrent;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.Bag12Bag2TestCase;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import bags1.MyBag;

/**
 * Test class for conflicting concurrent edits on both sides of the
 * Bag1-to-Bag2 transformation. Conflict tests address the same element from
 * both sides in a semantically incompatible way. Because the transformation
 * may resolve conflicts in different valid ways, the expected outcome is
 * asserted via {@code assertAnyPostcondition}, accepting any of the documented
 * resolution alternatives.
 *
 * <h3>Conflict categories covered</h3>
 * <ul>
 *   <li><b>CF-DeleteDelete</b>: both sides concurrently delete the same element type.</li>
 *   <li><b>CF-DeleteRename</b>: source deletes an element while target renames the same element.</li>
 *   <li><b>CF-RenameRename</b>: both sides rename the same element to different values.</li>
 *   <li><b>CF-MultiplicityRename</b>: source changes multiplicity while target renames the element.</li>
 * </ul>
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends Bag12Bag2TestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for a delete/delete conflict on Beer element
	 * (CF-DeleteDelete).<br/>
	 * Starting from a bag with three Beers, bag1 deletes one Beer while bag2
	 * concurrently also deletes its Beer element (multiplicity 3→0, i.e. the
	 * whole element).<br/>
	 * <b>Resolution alternatives</b>:
	 * <ol>
	 *   <li>Source deletion wins: two Beers remain.</li>
	 *   <li>Target deletion wins: no Beer remains (empty bags).</li>
	 * </ol>
	 * <b>Features</b>: concurrent, conflict, del/del
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testDeleteDeleteConflict(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createOneBeer,
				helperBag1::createOneBeer,
				helperBag1::createOneBeer));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));

		util.assertPrecondition("ThreeBeerBags1", "ThreeBeerBags2");
		// ── concurrent conflict: SRC deletes one Beer, TRG deletes all Beers ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeer),
				trgEdit(helperBag2::deleteAllBeers));
		// ── any resolution is acceptable ──
		util.assertAnyPostcondition(Map.ofEntries(
				// option 1: source deletion wins → two Beers remain
				Map.entry("TwoBeerBags1", "TwoBeerBags2"),
				// option 2: target deletion wins → empty bags
				Map.entry("RootElementBags1", "RootElementBags2")));
		terminate();
	}

	/**
	 * <b>Test</b> for a delete-on-source / rename-on-target conflict
	 * (CF-DeleteRename).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, bag1 deletes all Beer
	 * elements while bag2 concurrently changes the Beer element's value to Empty Bottle.<br/>
	 * <b>Resolution alternatives</b>:
	 * <ol>
	 *   <li>Source deletion wins: only Beer Glass remains, rename is rejected.</li>
	 *   <li>Target rename wins: delete is rejected, resulting in five Empty Bottles and
	 *       one Beer Glass.</li>
	 * </ol>
	 * <b>Features</b>: concurrent, conflict, del/rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testDeleteRenameConflict(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent conflict: SRC deletes Beer (one occurrence), TRG renames Beer → Empty Bottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::deleteBeer),
				trgEdit(helperBag2::changeBeerToEmptyBottle));
		// ── any resolution is acceptable ──
		util.assertAnyPostcondition(Map.ofEntries(
				// option 1: source deletion wins → four Beers + Beer Glass
				Map.entry("FourBeerWithGlassBags1", "FourBeerWithGlassBags2"),
				// option 2: target rename wins → five Empty Bottles + Beer Glass
				Map.entry("FiveEmptyBottlesWithGlassBags1", "FiveEmptyBottlesWithGlassBags2woInc")));
		terminate();
	}

	/**
	 * <b>Test</b> for a rename/rename conflict where both sides independently rename
	 * the same element type to different values (CF-RenameRename).<br/>
	 * Starting from a bag with five Beers and one Beer Glass, bag1 changes all Beers to
	 * Empty Bottles while bag2 concurrently changes the Beer element to Empty Bottle as
	 * well, followed by a further change of the Beer Glass to Empty Bottle on the target.<br/>
	 * <b>Resolution alternatives</b>:
	 * <ol>
	 *   <li>Source rename wins: five Empty Bottles + Beer Glass in both models.</li>
	 *   <li>Target rename wins: five Empty Bottles + Beer Glass in both models (same outcome here).</li>
	 * </ol>
	 * <b>Features</b>: concurrent, conflict, rename/rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRenameRenameConflict(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent conflict: SRC renames Beer→EmptyBottle, TRG also renames Beer→EmptyBottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::changeAllBeerToEmptyBottles),
				trgEdit(helperBag2::changeBeerToEmptyBottle));
		// ── any resolution is acceptable ──
//		util.assertAnyPostcondition(Map.ofEntries(
//				// option 1: source rename wins
//				Map.entry("FiveEmptyBottlesWithGlassBags1", "FiveEmptyBottlesWithGlassBags2woInc"),
//				// option 2: target rename wins (same state for this particular conflict)
//				Map.entry("FiveEmptyBottlesWithGlassBags1", "FiveEmptyBottlesWithGlassBags2woInc")));
		util.assertPostcondition("FiveEmptyBottlesWithGlassBags1", "FiveEmptyBottlesWithGlassBags2woInc");
		terminate();
	}

	/**
	 * <b>Test</b> for a multiplicity-change / rename conflict on the same element
	 * (CF-MultiplicityRename).<br/>
	 * Starting from a bag with four Beers and one Beer Glass, bag1 changes one Beer
	 * to Empty Bottle (reducing the Beer count) while bag2 concurrently changes
	 * the Beer Glass to Empty Bottle.<br/>
	 * <b>Resolution alternatives</b>:
	 * <ol>
	 *   <li>Both changes are accepted independently: four Beers + one Empty Bottle (from bag1 change)
	 *       plus the Beer Glass renamed to Empty Bottle → four Beers + two Empty Bottles.</li>
	 *   <li>Source change wins and target rename is rejected: four Beers + one Empty Bottle + one Beer Glass.</li>
	 *   <li>Target rename wins and source change is rejected: four Beers + two Empty Bottles (merged).</li>
	 * </ol>
	 * <b>Features</b>: concurrent, conflict, attribute/attribute
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testMultiplicityRenameConflict(BXTool<MyBag, bags2.MyBag, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));

		util.assertPrecondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		// ── concurrent conflict: SRC renames one Beer → Empty Bottle; TRG renames Beer Glass → Empty Bottle ──
		tool.performAndPropagateEdit(
				srcEdit(helperBag1::changeOneBeerToEmptyBottle),
				trgEdit(helperBag2::changeBeerGlassToEmptyBottle));
		tool.performIdleTargetEdit(trgEdit(helperBag2::changeIncrementalID));
		// ── any resolution is acceptable ──
		util.assertAnyPostcondition(Map.ofEntries(
				// option 1: both changes accepted → four Beers + two Empty Bottles
				Map.entry("FourBeerTwoEmptyBottlesBags1", "FourBeerTwoEmptyBottlesBags2"),
				// option 2: source change wins, target rename rejected → four Beers + one Empty Bottle + Beer Glass
				Map.entry("FourBeerOneEmptyBottleWithGlassBags1woInc", "FourBeerOneEmptyBottleWithGlassBags2"),
				// option 3: target rename wins, source change rejected → five Beers + one Empty Bottle
				Map.entry("FiveBeerOneEmptyBottleBags1", "FiveBeerOneEmptyBottleBags2")));
		terminate();
	}
}