package org.benchmarx.examples.pn2pnw.testsuite.batch.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class BatchForward extends Pn2PnwTestCase {

	public BatchForward() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for agreed upon starting state.<br/>
	 * <b>Expect</b> root elements of both source and target models.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<pn.Net, pnw.Net, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		util.assertPostcondition("EmptyPn", "EmptyPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for name change of an empty net.<br/>
	 * <b>Expect</b> corresponding name change in target net.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testNetNameChangeOfEmpty(BXTool<pn.Net, pnw.Net, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::renameToLettersAndDigits));

		util.assertPrecondition("EmptyLettersDigitsPn", "EmptyLettersDigitsPnw");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::renameToFactoryModel));
		//------------
		util.assertPostcondition("EmptyFactoryModelPn", "EmptyFactoryModelPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for creation of a simple net.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding net in the target model.
	 * <br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateSimpleNet(BXTool<pn.Net, pnw.Net, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createSimpleLettersDigits));
		//------------
		util.assertPostcondition("SimpleLettersDigitsPn", "SimpleLettersDigitsPnw");
		terminate();
	}

	/**
	 * Analogous to @link {@link #testCreateSimpleNet()}, but now with a more sophisticated net.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateComplexNet(BXTool<pn.Net, pnw.Net, Decisions> tool){
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperPn::createComplexLettersDigits));
		//------------
		util.assertPostcondition("ComplexLettersDigitsPn", "ComplexLettersDigitsPnw");
		terminate();
	}
}

