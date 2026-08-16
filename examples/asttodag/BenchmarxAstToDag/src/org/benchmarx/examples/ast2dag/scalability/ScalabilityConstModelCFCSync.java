package org.benchmarx.examples.ast2dag.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.benchmarx.examples.ast2dag.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of the source growing the existing
 * expression chain by more leaves while the target stays idle (see
 * {@link MonotonicCreating#testConcurrentSourceAddBestDigitTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_DIGITS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createBestDigitsAndConflictFreeChanges(int nrOfDigits, int nrOfEditedDigits) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedDigits, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperAst.createNBestDigits(nrOfDigits)),
								srcEdit(() -> helperAst.extendBestDigitsChainBy(nrOfDigits, nrOfEditedDigits)),
								trgEdit(helperDag::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000003ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000005ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000010ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000020ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000030ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000040ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000050ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000060ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000070ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000080ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000090ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateDigitsAndCreate0000100ConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(NR_OF_DIGITS, 100); }
}
