package org.benchmarx.examples.set2oset.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.concurrent.MonotonicCreating;
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
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of the source adding new elements
 * while the target stays idle (see
 * {@link MonotonicCreating#testConcurrentSourceAddTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_ELEMENTS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

	public static Collection<BXTool<MySet, osets.MyOrderedSet, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createElementsAndConflictFreeChanges(int nrOfElements, int nrOfEditedElements) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedElements, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperSet::setSetName, () -> helperSet.createNElements(nrOfElements, "E")),
								srcEdit(() -> helperSet.createNElements(nrOfEditedElements, "F")),
								trgEdit(helperOset::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000003ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000005ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000010ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000020ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000030ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000040ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000050ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000060ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000070ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000080ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000090ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateElementsAndCreate0000100ConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(NR_OF_ELEMENTS, 100); }
}
