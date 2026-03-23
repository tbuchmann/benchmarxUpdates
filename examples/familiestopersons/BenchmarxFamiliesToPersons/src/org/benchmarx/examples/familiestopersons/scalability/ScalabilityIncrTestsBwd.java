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
public class ScalabilityIncrTestsBwd extends ScalabilityTests {

	public ScalabilityIncrTestsBwd(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "INCR_BWD_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { }

	private void createOnePerson(int nrOfFamilies) {
		var timer = new BXToolTimer<>(tool, REPEAT);
		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfFamilies,
					timer.timeTargetEditAfterSetUpInS(
							trgEdit(() -> helperPerson.createPersons(nrOfFamilies, 5)),
							trgEdit(() -> helperPerson.createOnePerson())));
		});
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

	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0000500Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(500); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(1000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(10000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Persons(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(100000); }
	@Disabled @ParameterizedTest @MethodSource("tools")
	public void testCreate1000000FamiliesWithMembers(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createOnePerson(1000000); }
}