package org.benchmarx.examples.pdb12pdb2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pdb1.Database;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<Database, pdb2.Database, Decisions>> tools() {
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

	private void createPersons(int nrOfPersons) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfPersons, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(helperPerson1::setDatabaseName, () -> helperPerson1.createNPersons(nrOfPersons, "P"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Persons(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersons(1000000); }
}
