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
public class ScalabilityBatchTestsBwd extends ScalabilityTests {

	public ScalabilityBatchTestsBwd(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "BWD_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { /* results saved per test in external runners */ }

	private void createPersons(int nrOfFamilies) {
		var timer = new BXToolTimer<>(tool, REPEAT);
		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfFamilies,
					timer.timeTargetEditFromScratchInS(trgEdit(() -> helperPerson.createPersons(nrOfFamilies, 5))));
		});
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(100); }

	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(300); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0000500Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(500); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(1000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0003000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(3000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(5000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(10000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(100000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createPersons(1000000); }
}