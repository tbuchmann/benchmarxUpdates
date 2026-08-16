package org.benchmarx.examples.set2oset.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sets.MySet;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testConcurrentDeleteASrcRenameATrgConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_ELEMENTS = 3;

	public ScalabilityConstDeltaCSync() { super("CDCsync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(helperSet::setSetName, () -> helperSet.createNElements(nrOfElements, "E")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfElements,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperSet.deleteNElements(nrOfEditedElements, "E")),
						trgEdit(() -> helperOset.renameNElementsToZ(nrOfEditedElements, "E"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(3, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(5, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(10, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(20, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(30, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(40, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(50, NR_OF_EDITED_ELEMENTS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(1000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(5000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(10000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(50000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(100000, NR_OF_EDITED_ELEMENTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(500000, NR_OF_EDITED_ELEMENTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ElementsAndConflictingChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictingChanges(1000000, NR_OF_EDITED_ELEMENTS); }
}
