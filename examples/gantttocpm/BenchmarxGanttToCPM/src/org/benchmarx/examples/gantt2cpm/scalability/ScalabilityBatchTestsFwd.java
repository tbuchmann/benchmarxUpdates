package org.benchmarx.examples.gantt2cpm.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cpm.CPMNetwork;
import gantt.GanttDiagram;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	@AfterAll
	static void teardown() throws FileNotFoundException {
		// results saved per-run in individual tools
	}

	private void createActivities(int nrOfActivities) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfActivities, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(() -> helperGantt.createNActivities(nrOfActivities, "A"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Activities(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivities(1000000); }
}
