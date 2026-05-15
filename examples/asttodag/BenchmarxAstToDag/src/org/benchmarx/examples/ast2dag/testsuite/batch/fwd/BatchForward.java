package org.benchmarx.examples.ast2dag.testsuite.batch.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.Ast2DagTestCase;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class BatchForward extends Ast2DagTestCase {

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for agreed upon starting state.<br/>
	 * <b>Expect</b> root elements of both source and target models.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		util.assertPostcondition("RootElementAst", "RootElementDag");
	}
	
	/**
	 * <b>Test</b> for creation of a Expression with only one Number in an empty Ast.
	 * <br/>
	 * <b>Expect</b> Expression with only one number should be created.
	 * <br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateSingleExpression(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::create42));
		//------------
		util.assertPostcondition("42Ast", "42Dag");
	}

	/**
	 * Analogous to @link {@link #testCreateSingleExpression()}, but now for
	 * multiple Expressions.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateMultipleExpressions(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::createTextSum));
		//------------
		util.assertPostcondition("HG2GAst", "HG2GDag");
	}
	
	/**
	 * Analogous to @link {@link #testCreateSingleExpression()}, but now for
	 * multiple Expressions. This time the Expressions will be there more than one time.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateMultipleExpressionsComplex(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::createComplexNumberExample));
		//------------
		util.assertPostcondition("42ByMultiplyAddSubtractDivideAst", "42ByMultiplyAddSubtractDivideDag");
	}
	
	/**
	 * Analogous to @link {@link #testCreateSingleExpression()}, but now for
	 * multiple Expressions in multiple identical subtrees.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateMultipleSameSubtrees(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::createMulitpleSubtrees));
		//------------
		util.assertPostcondition("42ByMultipleSubteesAst", "42ByMultipleSubtreesDag");
	}
}
