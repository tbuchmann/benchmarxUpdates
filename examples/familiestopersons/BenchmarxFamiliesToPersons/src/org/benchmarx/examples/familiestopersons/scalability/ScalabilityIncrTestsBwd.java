package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import Families.FamilyRegister;
import Persons.PersonRegister;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityIncrTestsBwd extends ScalabilityTests {

	public ScalabilityIncrTestsBwd() { super("INCR_BWD_"); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { }

	private void createOnePerson(int nrOfFamilies) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareTargetEditAfterSetUp(trgEdit(() -> helperPerson.createPersons(nrOfFamilies, 5)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfFamilies,
						timer.measureTargetEditAfterSetUpInS(
						trgEdit(() -> helperPerson.createOnePerson())));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(1000000); }
}
