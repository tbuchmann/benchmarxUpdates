package org.benchmarx.examples.bag12bag2.testsuite.batch.bwd;

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
public class BatchBackward extends Bag12Bag2TestCase {

	public BatchBackward() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for creation of a single Element (Beer) in an empty Database.
	 * <br/>
	 * <b>Expect</b> 1 Beer to be created in the source model.
	 * <br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateElement(BXTool<MyBag, bags2.MyBag, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperBag2::createOneBeer));
		//------------
		util.assertPostcondition("OneBeerBags1", "OneBeerBags2");
		terminate();
	}

	/**
	 * Analogous to @link {@link #testCreateElement()}, but now for
	 * multiple Elements (5 Beers and 1 Beer Glass).<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateMultipleElements(BXTool<MyBag, bags2.MyBag, Decisions> tool){
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperBag2::createFiveBeer,
				helperBag2::createBeerGlass));
		//------------
		util.assertPostcondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		terminate();
	}
}