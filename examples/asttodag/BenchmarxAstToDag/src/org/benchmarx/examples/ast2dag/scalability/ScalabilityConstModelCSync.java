package org.benchmarx.examples.ast2dag.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.benchmarx.examples.ast2dag.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. Since
 * this example's genuine conflict ({@link Conflicts#testConcurrentRenameSharedVariableConflict})
 * targets a single shared variable with no per-element identity to repeat, the
 * conflicting-change count is instead scaled by bundling n concurrent
 * incrementalID attribute toggles into a single concurrent sync step on both
 * sides - still exercising n atomic conflicting edit steps per measurement.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_DIGITS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

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
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedTimes, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperAst.createNBestDigits(nrOfDigits)),
								srcEdit(() -> helperAst.changeIncrementalIDNTimes(nrOfEditedTimes)),
								trgEdit(() -> helperDag.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000003ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000005ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000010ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000020ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000030ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000040ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAnd0000050ConflictingChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictingChanges(NR_OF_DIGITS, 50); }
}
