package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.examples.familiestopersons.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Collection;

import Families.FamilyRegister;
import Persons.PersonRegister;


@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
 /* This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes. The
 * families are created in pairs of two (Simpson and Flanders). A conflicting
 * change consists of a relocation of Lisa to the family Flanders and a deletion
 * of Lisa in the person register (see {@link Conflicts}.
 */
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_FAMILY_PAIRS = 3;

	public ScalabilityConstDeltaCSync(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "CDCsync_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	private void createFamilyPairsAndConflictingChanges(int nrOfFamilyPairs, int nrOfEditedFamilyPairs) {
		var timer = new BXToolTimer<>(tool, REPEAT);

		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfFamilyPairs, //
					timer.timeEditAfterSetUpInS(
							srcEdit(() -> {
								helperFamily.createSimpsonFamiliesWithMembers(nrOfFamilyPairs);
								helperFamily.createFlandersFamiliesWithMembers(nrOfFamilyPairs);
								}),
							srcEdit(() -> helperFamily.moveLisaToFlandersAsDaugther(nrOfEditedFamilyPairs)),
							trgEdit(() -> helperPerson.deleteLisa(nrOfEditedFamilyPairs))));
		});
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(3, NR_OF_EDITED_FAMILY_PAIRS);
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(5, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(10, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(20, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(30, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(40, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(50, NR_OF_EDITED_FAMILY_PAIRS);
	}
	
}
