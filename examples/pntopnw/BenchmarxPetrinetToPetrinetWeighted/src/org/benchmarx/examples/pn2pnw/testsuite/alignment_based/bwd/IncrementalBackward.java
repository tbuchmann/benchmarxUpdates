package org.benchmarx.examples.pn2pnw.testsuite.alignment_based.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends Pn2PnwTestCase {

	public IncrementalBackward() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting of a place, a transition and some edges into an existing pnw, after the initial pnw has
	 * been transformed into a pn. <br/>
	 * <b>Expect</b> : New elements are added to the net, while the old elements remain unchanged. <br/>
	 * <b>Features</b>: bwd, add, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalInserts(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::createPTPLettersDigits,
				helperPnw::weightA1BWith42));
		tool.performIdleSourceEdit(srcEdit(helperPn::changeIncrementalID));
		
		util.assertPrecondition("PTPLettersDigitsChangedPn", "PTPLettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::extendPTPLettersDigits));
		//------------
		util.assertPostcondition("PTPExtendedLettersDigitsChangedPn", "PTPExtendedLettersDigitsWeightedPnw");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::furtherExtendPTPLettersDigits));
		util.assertPostcondition("PTPFurtherExtendedLettersDigitsChangedPn", "PTPFurtherExtendedLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for deleting a place, a transition and some edges into an existing pnw, after the initial pnw has
	 * been transformed into a pn. <br/>
	 * <b>Expect</b>: Deletion of the correct elements, while the other elements remain unchanged. <br/>
	 * <b>Features</b>: bwd, del, corr-based, structural
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalDeletions(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPnw::createPTPLettersDigits,
				helperPnw::extendPTPLettersDigits,
				helperPnw::furtherExtendPTPLettersDigits,
				helperPnw::weightA1BWith42));
		tool.performIdleSourceEdit(srcEdit(helperPn::changeIncrementalID));
		
		util.assertPrecondition("PTPFurtherExtendedLettersDigitsChangedPn", "PTPFurtherExtendedLettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::reducePTPFurtherExtendedLettersDigits));
		//------------
		util.assertPostcondition("PTPExtendedLettersDigitsChangedPn", "PTPExtendedLettersDigitsWeightedPnw");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::reducePTPExtendedLettersDigits));
		util.assertPostcondition("PTPLettersDigitsChangedPn", "PTPLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for changing the tokens and edges of an existing pnw, after the initial pnw has been transformed
	 * into a pn. <br/>
	 * <b>Expect</b>: Change the respective tokens and edges in the pn, while the other elements remain
	 * unchanged. <br/>
	 * <b>Features</b>: bwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testIncrementalChanges(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPnw::create1234LettersDigits,
				helperPnw::weightA1BWith42,
				helperPnw::weightB2With9));
		tool.performIdleSourceEdit(srcEdit(helperPn::changeIncrementalID));
		
		util.assertPrecondition("1234LettersDigitsChangedPn", "1234LettersDigitsWeightedPnw");
		//------------
		tool.performIdleTargetEdit(trgEdit(helperPnw::weightA1BWith73));
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::construct9012LettersDigits));
		//------------
		util.assertPostcondition("9012LettersDigitsChangedPn", "9012LettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle target delta does not change the source model.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testStability(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::createComplexLettersDigits));
		tool.performIdleSourceEdit(srcEdit(helperPn::changeIncrementalID));
		
		util.assertPrecondition("ComplexLettersDigitsChangedPn", "ComplexLettersDigitsWeightedPnw");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::idleDelta));
		//------------
		util.assertPostcondition("ComplexLettersDigitsChangedPn", "ComplexLettersDigitsWeightedPnw");
		terminate();
	}

	@ParameterizedTest
	@MethodSource("tools")
	public void testHippocraticness(BXTool<pn.Net, pnw.Net, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperPnw::createPTPLettersDigits,
				helperPnw::weightA1BWith42));
		tool.performIdleSourceEdit(srcEdit(helperPn::changeIncrementalID));
		
		util.assertPrecondition("PTPLettersDigitsChangedPn", "PTPLettersDigitsWeightedPnw"); 
		// ---------------------------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::weightA1BWith73));
		// ---------------------------------
		util.assertPostcondition("PTPLettersDigitsChangedPn", "PTPLettersDigitsWeighted73Pnw"); 
		terminate();
	}
}

