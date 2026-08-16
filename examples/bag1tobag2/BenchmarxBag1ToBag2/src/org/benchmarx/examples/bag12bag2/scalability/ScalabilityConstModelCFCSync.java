package org.benchmarx.examples.bag12bag2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.benchmarx.examples.bag12bag2.testsuite.concurrent.MonotonicCreating;
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
 * changes. A conflict-free change consists of adding a new Beer Glass element
 * on the source side and a new Empty Bottle element on the target side (see
 * {@link MonotonicCreating#testConcurrentAddNonMatchingValues}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_BEERS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createBeersAndConflictFreeChanges(int nrOfBeers, int nrOfEditedBeers) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedBeers, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperBag1.createNBeers(nrOfBeers)),
								srcEdit(() -> helperBag1.createNBeerGlasses(nrOfEditedBeers)),
								trgEdit(() -> helperBag2.createNEmptyBottles(nrOfEditedBeers))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000003ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000005ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000010ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000020ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000030ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000040ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000050ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000060ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000070ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000080ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000090ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAndCreate0000100ConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(NR_OF_BEERS, 100); }
}
