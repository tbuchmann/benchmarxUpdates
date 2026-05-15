package org.benchmarx.examples.ast2dag.testsuite.alignment_based.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.Ast2DagTestCase;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends Ast2DagTestCase {

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting new nodes in an existing dag. Inserting a node is only possible at leaves. <br/>
	 * <b>Expect</b> : New nodes are inserted in the dag. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInserts(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::createBestDigit));
		tool.performIdleSourceEdit(srcEdit(helperAst::changeIncrementalID));
		
		util.assertPrecondition("BestDigitIncrIDAst", "BestDigitDag");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::insertMoreBestDigits));
		//------------
		util.assertPostcondition("MoreBestDigitsIncrIDAst", "MoreBestDigitsDag");
	}
	
	/**
	 * <b>Test</b> for deleting nodes from an existing dag. Only inner nodes can be deleted, deleting leaves would
	 * result in an invalid tree. Childs of the deleted node are deleted recursiv.
	 * <b>Expect</b>: Delete the correct nodes from an dag.
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::createMoreBestDigits));
		tool.performIdleSourceEdit(srcEdit(helperAst::changeIncrementalID));
		
		util.assertPrecondition("MoreBestDigitsAllIncrIDAst", "MoreBestDigitsDag");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::removeSomeBestDigits));
		//------------
		util.assertPostcondition("BestDigitIncrIDDelAst", "BestDigitDag");
	}
	
	/**
	 * <b>Test</b> for modifying an dag. Modifying is changing the value of a node or switch between variable and
	 *  number.
	 * <b>Expect</b>: Change some nodes in the dag.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalModifications(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::createBestDigitRef));
		tool.performIdleSourceEdit(srcEdit(helperAst::changeIncrementalID));
		
		util.assertPrecondition("BestDigitRefIncrIDAst", "BestDigitRefDag");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::modifyBestDigitRef));
		//------------
		util.assertPostcondition("BestDigitRefModifiedIncrIDAst", "BestDigitRefModifiedDag");
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::createBestDigit));
		tool.performIdleSourceEdit(srcEdit(helperAst::changeIncrementalID));
		//------------
		util.assertPostcondition("BestDigitIncrIDAst", "BestDigitDag");
		
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::idleDelta));
		util.assertPostcondition("BestDigitIncrIDAst", "BestDigitDag");
	}
}
