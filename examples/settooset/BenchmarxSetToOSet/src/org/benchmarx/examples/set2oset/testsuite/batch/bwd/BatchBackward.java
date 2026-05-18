package org.benchmarx.examples.set2oset.testsuite.batch.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.Set2OsetTestCase;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class BatchBackward extends Set2OsetTestCase {
	
	public BatchBackward() {
		super();
	}
	
	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		util.assertPostcondition("RootElementSet", "RootElementOset");
		terminate();
	}
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testDatabaseNameChangeOfEmpty(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::setSetName));

		util.assertPrecondition("EmptyAlphabetSet", "EmptyAlphabetOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::renameAlphabetSetToABC));
		//------------
		util.assertPostcondition("EmptyABCSet", "EmptyABCOset");
		terminate();
	}
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateElement(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::setSetName));

		util.assertPrecondition("EmptyAlphabetSet", "EmptyAlphabetOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::createA));
		//------------
		util.assertPostcondition("OnlyASet", "OnlyAOset");
		terminate();
	}

	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateMultipleElements(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::setSetName));

		util.assertPrecondition("EmptyAlphabetSet", "EmptyAlphabetOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::createA,
				helperOset::createB,
				helperOset::createC));
		//------------
		util.assertPostcondition("FirstThreeLettersSet", "FirstThreeLettersOset");
		terminate();
	}
}