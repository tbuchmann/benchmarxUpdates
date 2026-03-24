package org.benchmarx.examples.pn2pnw.testsuite.alignment_based.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalForward extends Pn2PnwTestCase {

	public IncrementalForward() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting of a place and a transition into an existing pn after the weights in the pnw have been
	 * changed. <br/>
	 * <b>Expect</b> : New elements are added to the net, while the old elements remain unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		
		util.assertPrecondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::extendPTPLettersDigits));
		//------------
		util.assertPostcondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for deleting of a place and a transition from an existing pn after the weights in the pnw have been
	 * changed. <br/>
	 * <b>Expect</b>: Deletion of the correct elements, while the other elements remain unchanged. <br/>
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPn::createPTPLettersDigits,
				helperPn::extendPTPLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith42));
		
		util.assertPrecondition("PTPExtendedLettersDigitsPn", "PTPExtendedLettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::reducePTPExtendedLettersDigits));
		//------------
		util.assertPostcondition("PTPLettersDigitsPn", "PTPLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for changing the tokens and edges of an existing pn after the weights in the pnw have been
	 * changed. <br/>
	 * <b>Expect</b>: Change the respective tokens and edges in the pnw, while the other elements remain
	 * unchanged. <br/>
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalChanges(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::create1234LettersDigits));
		tool.performIdleTargetEdit(trgEdit(
				helperPnw::weightA1BWith42,
				helperPnw::weightB2With9));
		
		util.assertPrecondition("1234LettersDigitsPn", "1234LettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::construct5678LettersDigits));
		//------------
		util.assertPostcondition("5678LettersDigitsPn", "5678LettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createComplexLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA3DWith24));
		
		util.assertPrecondition("ComplexLettersDigitsPn", "ComplexLettersDigitsSimpleWeightedPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::idleDelta));
		//------------
		util.assertPostcondition("ComplexLettersDigitsPn", "ComplexLettersDigitsSimpleWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after changing the incrementalID does not change the pnw.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testHipporcraticness(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createComplexLettersDigits));
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA3DWith24));
		
		util.assertPrecondition("ComplexLettersDigitsPn", "ComplexLettersDigitsSimpleWeightedPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::changeIncrementalID));
		//------------
		util.assertPostcondition("ComplexLettersDigitsChangedPn", "ComplexLettersDigitsSimpleWeightedPnw");
		terminate();
	}
}

