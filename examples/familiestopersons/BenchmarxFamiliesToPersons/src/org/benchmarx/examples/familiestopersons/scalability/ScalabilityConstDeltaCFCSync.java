package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.examples.familiestopersons.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.examples.familiestopersons.testsuite.concurrent.MonotonicDeleting;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import Families.FamilyRegister;
import Persons.PersonRegister;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes. A conflict-free change consists of a new family member Hugo (son) to
 * the family Simpson and a deletion of Lisa in the person register (see
 * {@link MonotonicCreating} and {@link MonotonicDeleting}.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_FAMILIES = 3;

	public ScalabilityConstDeltaCFCSync(BXTool<FamilyRegister, PersonRegister, Decisions> tool) {
		super(tool, "CDCFCSync_");
	}

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	private void createFamiliesAndConflictFreeChanges(int nrOfFamilyPairs, int nrOfEditedFamilyPairs) {
		var timer = new BXToolTimer<>(tool, REPEAT);

		assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT * REPEAT), () -> {
			results.put(nrOfFamilyPairs, //
					timer.timeEditAfterSetUpInS(
							srcEdit(() -> helperFamily.createSimpsonFamiliesWithMembers(nrOfFamilyPairs)),
							srcEdit(() -> helperFamily.createSonHugo(nrOfEditedFamilyPairs)),
							trgEdit(() -> helperPerson.deleteLisa(nrOfEditedFamilyPairs))));
		});
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(3, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(5, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(10, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(20, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(30, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(40, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(50, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(60, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(70, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(80, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(90, NR_OF_EDITED_FAMILIES);
	}
	
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100FamiliesAndConflictFreeChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); } {
		createFamiliesAndConflictFreeChanges(100, NR_OF_EDITED_FAMILIES);
	}
}
