package org.benchmarx.examples.pn2pnw.testsuite.batch.bwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.Pn2PnwTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class BatchBackward extends Pn2PnwTestCase {

	public BatchBackward() {
		super();
	}

	public static Collection<BXTool<pn.Net, pnw.Net, Decisions>> tools() {
		return Pn2PnwTestCase.tools();
	}

	/**
	 * <b>Test</b> for name change of an empty net.<br/>
	 * <b>Expect</b> corresponding name change in source net.<br/>
	 * <b>Features</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testNetNameChangeOfEmpty(BXTool<pn.Net, pnw.Net, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::renameToLettersAndDigits));

		util.assertPrecondition("EmptyLettersDigitsPn", "EmptyLettersDigitsPnw");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::renameToFactoryModel));
		//------------
		util.assertPostcondition("EmptyFactoryModelPn", "EmptyFactoryModelPnw");
		terminate();
	}

	/**
	 * <b>Test</b> for creation of a simple net.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding net in the source model.
	 * <br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateSimpleNet(BXTool<pn.Net, pnw.Net, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::createSimpleLettersDigits));
		//------------
		util.assertPostcondition("SimpleLettersDigitsPn", "SimpleLettersDigitsWeightedPnw");
		terminate();
	}

	/**
	 * Analogous to @link {@link #testCreateSimpleNet()}, but now with a more sophisticated net.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateComplexNet(BXTool<pn.Net, pnw.Net, Decisions> tool){
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperPnw::createComplexLettersDigits));
		//------------
		util.assertPostcondition("ComplexLettersDigitsPn", "ComplexLettersDigitsWeightedPnw");
		terminate();
	}
}

