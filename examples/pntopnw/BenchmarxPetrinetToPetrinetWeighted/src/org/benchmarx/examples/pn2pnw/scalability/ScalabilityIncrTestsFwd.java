package org.benchmarx.examples.pn2pnw.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pn.Net;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityIncrTestsFwd extends ScalabilityTests {

	public ScalabilityIncrTestsFwd() { super("INCR_FWD_"); }

	public static Collection<BXTool<Net, pnw.Net, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { }

	private void createOneMorePTPUnit(int nrOfUnits) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(helperPn::renameToLettersAndDigits, () -> helperPn.createNPTPUnits(nrOfUnits, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfUnits,
						timer.measureSourceEditAfterSetUpInS(
						srcEdit(() -> helperPn.createNPTPUnits(1, "X"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000PTPUnits(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createOneMorePTPUnit(1000000); }
}
