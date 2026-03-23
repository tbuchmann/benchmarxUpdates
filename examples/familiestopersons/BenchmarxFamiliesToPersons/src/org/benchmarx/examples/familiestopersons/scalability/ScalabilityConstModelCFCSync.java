package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.examples.familiestopersons.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.examples.familiestopersons.testsuite.concurrent.MonotonicDeleting;
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
 * with a constant model size and a growing number of conflict-free (CF) changes. A conflict-free
 * change consists of a new family member Hugo (son) to the family Simpson and a deletion
 * of Lisa in the person register (see {@link MonotonicCreating} and {@link MonotonicDeleting}.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_FAMILY_PAIRS = 100;

	public ScalabilityConstModelCFCSync(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "CMCFCSync_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	private void createFamiliesAndConflictingChanges(int nrOfFamilyPairs, int nrOfEditedFamilyPairs) {
		var timer = new BXToolTimer<>(tool, REPEAT);

		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfEditedFamilyPairs, //
					timer.timeEditAfterSetUpInS(
							srcEdit(() -> helperFamily.createSimpsonFamiliesWithMembers(nrOfFamilyPairs)),
							srcEdit(() -> helperFamily.createSonHugo(nrOfEditedFamilyPairs)),
							trgEdit(() -> helperPerson.deleteLisa(nrOfEditedFamilyPairs))));
		});
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000003ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 3);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000005ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 5);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000010ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 10);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000020ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 20);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000030ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 30);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000040ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 40);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000050ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 50);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000060ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 60);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000070ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 70);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000080ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 80);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000090ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 90);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreateFamiliesAndCreate0000100ConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictingChanges(NR_OF_FAMILY_PAIRS, 100);
	}
}
