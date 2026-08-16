package org.benchmarx.examples.ecore2sql.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
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

	private void createLists(int nrOfLists) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfLists, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(() -> helperEcore.createNListsWithLength(nrOfLists, "List"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Lists(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createLists(1000000); }
}
