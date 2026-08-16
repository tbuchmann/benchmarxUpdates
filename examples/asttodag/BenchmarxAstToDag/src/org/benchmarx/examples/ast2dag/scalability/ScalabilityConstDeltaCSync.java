package org.benchmarx.examples.ast2dag.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.benchmarx.examples.ast2dag.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testConcurrentRenameSharedVariableConflict} and
 * {@link ScalabilityConstModelCSync} for the scaling rationale).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_TIMES = 3;

	public ScalabilityConstDeltaCSync() { super("CDCSync_"); }

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createBestDigitsAndConflictingChanges(int nrOfDigits, int nrOfEditedTimes) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperAst.createNBestDigits(nrOfDigits)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfDigits,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperAst.changeIncrementalIDNTimes(nrOfEditedTimes)),
						trgEdit(() -> helperDag.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(3, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(5, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(10, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(20, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(30, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(40, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(50, NR_OF_EDITED_TIMES); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(1000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(5000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(10000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(50000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(100000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(500000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000DigitsAndConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(1000000, NR_OF_EDITED_TIMES); }
}
