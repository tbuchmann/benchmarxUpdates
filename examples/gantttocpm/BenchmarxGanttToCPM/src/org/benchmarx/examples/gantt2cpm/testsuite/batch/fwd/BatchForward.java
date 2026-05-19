package org.benchmarx.examples.gantt2cpm.testsuite.batch.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.GanttToCPMTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cpm.CPMNetwork;
import gantt.GanttDiagram;

@ExtendWith(BXToolParameterResolver.class)
public class BatchForward extends GanttToCPMTestCase {

	public BatchForward() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for agreed upon starting state.<br/>
	 * <b>Expect</b> root elements of both source and target models.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		util.assertPostcondition("RootElementGantt", "RootElementCpm");
	}

	/**
	 * <b>Test</b> for name change of an empty gantt diagram.<br/>
	 * <b>Expect</b> name in the cpm network is also changed.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testGanttNameChangeOfEmpty(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::createEmptyGantt2CPMProcedure));

		util.assertPrecondition("EmptyGantt2CpmGantt", "EmptyGantt2CpmCpm");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::createEmptyItalyTankRush));
		//------------
		util.assertPostcondition("EmptyItalyTankRushGantt", "EmptyItalyTankRushCpm");
	}

	/**
	 * <b>Test</b> for creation of a simple gantt diagram.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding cpm network.
	 * <br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateGantt(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::createSimpleTankRush));
		//------------
		util.assertPostcondition("SimpleTankRushGantt", "SimpleTankRushCpm");
	}

	/**
	 * Analogous to @link {@link #testCreateGantt()}, now with all possible dependency types.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateComplexGantt(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::createComplexTankRush));
		//------------
		util.assertPostcondition("ComplexTankRushGantt", "ComplexTankRushCpm");
	}
}