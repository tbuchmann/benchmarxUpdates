package org.benchmarx.examples.ast2dag.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.benchmarx.examples.ast2dag.testsuite.concurrent.MonotonicCreating;
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
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link MonotonicCreating#testConcurrentSourceAddBestDigitTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_DIGITS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperAst.createNBestDigits(nrOfDigits)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfDigits,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperAst.extendBestDigitsChainBy(nrOfDigits, nrOfEditedDigits)),
						trgEdit(helperDag::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(3, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(5, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(10, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(20, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(30, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(40, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(50, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(60, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(70, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(80, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(90, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(100, NR_OF_EDITED_DIGITS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(1000, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(5000, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(10000, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(50000, NR_OF_EDITED_DIGITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(100000, NR_OF_EDITED_DIGITS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(500000, NR_OF_EDITED_DIGITS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000DigitsAndConflictFreeChanges(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigitsAndConflictFreeChanges(1000000, NR_OF_EDITED_DIGITS); }
}
