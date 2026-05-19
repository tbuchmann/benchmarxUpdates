package org.benchmarx.examples.gantt2cpm.testsuite.batch.bwd;

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
public class BatchBackward extends GanttToCPMTestCase {

	public BatchBackward() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for name change of an empty cpm network.<br/>
	 * <b>Expect</b> name in the gantt diagram is also changed.<br/>
	 * <b>Features</b>: bwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCpmNameChangeOfEmpty(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::createEmptyGantt2CPMProcedure));

		util.assertPrecondition("EmptyGantt2CpmGantt", "EmptyGantt2CpmCpm");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::createEmptyItalyTankRush));
		//------------
		util.assertPostcondition("EmptyItalyTankRushGantt", "EmptyItalyTankRushCpm");
	}

	/**
	 * <b>Test</b> for creation of a simple cpm network.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding gantt diagram.
	 * <br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateCpm(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::createSimpleTankRush));
		//------------
		util.assertPostcondition("SimpleTankRushGantt", "SimpleTankRushCpm");
	}

	/**
	 * Analogous to @link {@link #testCreateCpm()}, now with all possible dependency types.<br/>
	 * <b>Features:</b>: bwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCreateComplexGantt(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { // rename to CPM?
		this.tool = tool; initialise();
		// No precondition!
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::createComplexTankRush));
		//------------
		util.assertPostcondition("ComplexTankRushGantt", "ComplexTankRushCpm");
	}
}