package org.benchmarx.examples.set2oset.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sets.MySet;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<MySet, osets.MyOrderedSet, Decisions>> tools() {
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

	private void createElements(int nrOfElements) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfElements, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(helperSet::setSetName, () -> helperSet.createNElements(nrOfElements, "E"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Elements(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElements(1000000); }
}
