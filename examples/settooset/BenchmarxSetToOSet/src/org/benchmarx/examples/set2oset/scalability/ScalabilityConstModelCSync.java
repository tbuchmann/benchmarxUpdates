package org.benchmarx.examples.set2oset.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sets.MySet;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. A
 * conflicting change consists of deleting one element on the source side while
 * concurrently renaming that same element on the target side (see
 * {@link Conflicts#testConcurrentDeleteASrcRenameATrgConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_ELEMENTS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

	public static Collection<BXTool<MySet, osets.MyOrderedSet, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createElementsAndConflictingChanges(int nrOfElements, int nrOfEditedElements) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedElements, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperSet::setSetName, () -> helperSet.createNElements(nrOfElements, "E")),
								srcEdit(() -> helperSet.deleteNElements(nrOfEditedElements, "E")),
								trgEdit(() -> helperOset.renameNElementsToZ(nrOfEditedElements, "E"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000003ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000005ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000010ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000020ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000030ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000040ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAnd0000050ConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(NR_OF_ELEMENTS, 50); }
}
