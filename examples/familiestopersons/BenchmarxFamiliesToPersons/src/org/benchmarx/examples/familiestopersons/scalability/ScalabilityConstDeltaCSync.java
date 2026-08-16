package org.benchmarx.examples.familiestopersons.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.familiestopersons.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
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
 /* This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes. The
 * families are created in pairs of two (Simpson and Flanders). A conflicting
 * change consists of a relocation of Lisa to the family Flanders and a deletion
 * of Lisa in the person register (see {@link Conflicts}.
 */
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_FAMILY_PAIRS = 3;

	public ScalabilityConstDeltaCSync() { super("CDCsync_"); }

	public static Collection<BXTool<FamilyRegister, PersonRegister, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createFamilyPairsAndConflictingChanges(int nrOfFamilyPairs, int nrOfEditedFamilyPairs) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> {
									helperFamily.createSimpsonFamiliesWithMembers(nrOfFamilyPairs);
									helperFamily.createFlandersFamiliesWithMembers(nrOfFamilyPairs);
									}));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfFamilyPairs,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperFamily.moveLisaToFlandersAsDaugther(nrOfEditedFamilyPairs)),
						trgEdit(() -> helperPerson.deleteLisa(nrOfEditedFamilyPairs))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(3, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(5, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(10, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(20, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(30, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(40, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(50, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(1000, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(5000, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(10000, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(50000, NR_OF_EDITED_FAMILY_PAIRS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(100000, NR_OF_EDITED_FAMILY_PAIRS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(500000, NR_OF_EDITED_FAMILY_PAIRS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000FamilyPairsAndConflictingChanges(BXTool<FamilyRegister, PersonRegister, Decisions> tool) { this.tool = tool; initialise(); createFamilyPairsAndConflictingChanges(1000000, NR_OF_EDITED_FAMILY_PAIRS); }
}
