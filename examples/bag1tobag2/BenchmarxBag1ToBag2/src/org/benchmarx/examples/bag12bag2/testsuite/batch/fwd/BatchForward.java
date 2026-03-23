package org.benchmarx.examples.bag12bag2.testsuite.batch.fwd;

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
public class BatchForward extends Bag12Bag2TestCase {

	public BatchForward() {
		super();
	}

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return Bag12Bag2TestCase.tools();
	}

	/**
	 * <b>Test</b> for agreed upon starting state.<br/>
	 * <b>Expect</b> root elements of both source and target models.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<MyBag, bags2.MyBag, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		util.assertPostcondition("RootElementBags1", "RootElementBags2");
		terminate();
	}
	
	/**
	 * <b>Test</b> for creation of a single Element (Beer) in an empty Database.
	 * <br/>
	 * <b>Expect</b> 1 Beer to be created in the target model.
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
		tool.performAndPropagateSourceEdit(srcEdit(helperBag1::createOneBeer));
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
		tool.performAndPropagateSourceEdit(srcEdit(
				helperBag1::createFiveBeers,
				helperBag1::createBeerGlass));
		//------------
		util.assertPostcondition("FiveBeerWithGlassBags1", "FiveBeerWithGlassBags2");
		terminate();
	}
}