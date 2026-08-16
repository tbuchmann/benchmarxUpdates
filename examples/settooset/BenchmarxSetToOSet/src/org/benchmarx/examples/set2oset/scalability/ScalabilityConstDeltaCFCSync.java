package org.benchmarx.examples.set2oset.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.concurrent.MonotonicCreating;
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
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link MonotonicCreating#testConcurrentSourceAddTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_ELEMENTS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(helperSet::setSetName, () -> helperSet.createNElements(nrOfElements, "E")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfElements,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperSet.createNElements(nrOfEditedElements, "F")),
						trgEdit(helperOset::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(3, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(5, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(10, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(20, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(30, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(40, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(50, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(60, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(70, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(80, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(90, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(100, NR_OF_EDITED_ELEMENTS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(1000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(5000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(10000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(50000, NR_OF_EDITED_ELEMENTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(100000, NR_OF_EDITED_ELEMENTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(500000, NR_OF_EDITED_ELEMENTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ElementsAndConflictFreeChanges(BXTool<MySet, osets.MyOrderedSet, Decisions> tool) { this.tool = tool; initialise(); createElementsAndConflictFreeChanges(1000000, NR_OF_EDITED_ELEMENTS); }
}
