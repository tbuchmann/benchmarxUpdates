package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import Families.FamilyRegister;
import Persons.PersonRegister;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	@AfterAll
	static void teardown(org.junit.jupiter.api.TestInfo testInfo) throws FileNotFoundException {
		// tool instance not available statically; results are written per-run in individual tools
	}

	private void createFamilies(int nrOfFamilies) {
		var timer = new BXToolTimer<>(tool, REPEAT);
		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfFamilies, //
					timer.timeSourceEditFromScratchInS(
							srcEdit(() -> helperFamily.createSimpsonFamiliesWithMembers(nrOfFamilies))));
		});
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(300); }

	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0000500FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(500); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0001000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(1000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate003000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(3000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate005000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(5000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0010000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(10000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0100000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(100000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate1000000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilies(1000000); }
}