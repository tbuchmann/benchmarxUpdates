package org.benchmarx.examples.set2oset.testsuite.alignment_based.bwd;

import java.io.IOException;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.Set2OsetTestCase;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends Set2OsetTestCase {
	
	public IncrementalBackward(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		super(tool);
	}
	
	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() throws IOException {
		return Set2OsetTestCase.tools();
	}
	
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) throws IOException {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::setSetName,
				helperOset::createC));
		tool.performIdleSourceEdit(srcEdit(helperSet::changeIncrementalID));
		
		util.assertPrecondition("CChangedSet", "COset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::insertABeforeC));
		//------------
		util.assertPostcondition("AcChangedSet", "AcOset");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::insertBBeforeC));
		util.assertPostcondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::insertDAfterC));
		util.assertPostcondition("AbcdChangedSet", "AbcdOset");
		terminate();
	}
	
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) throws IOException {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::setSetName,
				helperOset::createA,
				helperOset::createB,
				helperOset::createC,
				helperOset::createD));
		tool.performIdleSourceEdit(srcEdit(helperSet::changeIncrementalID));
		
		util.assertPrecondition("AbcdChangedSet", "AbcdOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::deleteD));
		//------------
		util.assertPostcondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::deleteB));
		util.assertPostcondition("AcChangedSet", "AcOset");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::deleteA));
		util.assertPostcondition("CChangedSet", "COset");
		terminate();
	}
	
	
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalValueChange(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) throws IOException {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::setSetName,
				helperOset::createA,
				helperOset::createB,
				helperOset::createC));
		tool.performIdleSourceEdit(srcEdit(helperSet::changeIncrementalID));
		
		util.assertPrecondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::changeABCtoZXY));
		//------------
		util.assertPostcondition("ZxyChangedSet", "ZxyOset");
		terminate();
	}

	
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) throws IOException {
		this.tool = tool;
		initialise();		
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::setSetName,
				helperOset::createA,
				helperOset::createB,
				helperOset::createC));
		tool.performIdleSourceEdit(srcEdit(helperSet::changeIncrementalID));

		util.assertPrecondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::idleDelta));
		//------------
		util.assertPostcondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
	}
	
	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after inverting the oset ABC to CBA should not change the
	 * set.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testHippocraticness() {
		tool.performAndPropagateTargetEdit(trgEdit(
				helperOset::setSetName,
				helperOset::createA,
				helperOset::createB,
				helperOset::createC));
		tool.performIdleSourceEdit(srcEdit(helperSet::changeIncrementalID));

		util.assertPrecondition("FirstThreeLettersChangedSet", "FirstThreeLettersOset");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperOset::invert));
		//------------
		util.assertPostcondition("FirstThreeLettersChangedSet", "CbaOset");
	}
}
