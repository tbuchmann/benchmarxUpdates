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

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. The
 * families are created in pairs of two (Simpson and Flanders). A conflicting
 * change consists of a relocation of Lisa to the family Flanders and a deletion
 * of Lisa in the person register (see {@link Conflicts}.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_FAMILY_PAIRS = 50;

	public ScalabilityConstModelCSync(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "CMCSync_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	private void createFamilyPairsAndConflictingChanges(int nrOfFamilyPairs, int nrOfEditedFamilyPairs) {
		var timer = new BXToolTimer<>(tool, REPEAT);

		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfEditedFamilyPairs, //
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
	public void testCreateFamilyPairsAnd0000003ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 3);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000005ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 5);
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000010ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 10);
	}
	

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000020ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 20);
	}
	

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000030ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 30);
	}
	

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000040ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 40);
	}
	

	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamilyPairsAnd0000050ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamilyPairsAndConflictingChanges(NR_OF_FAMILY_PAIRS, 50);
	}
	
}
