package org.benchmarx.examples.bag12bag2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.benchmarx.examples.bag12bag2.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. A
 * conflicting change consists of deleting one Beer element on the source side
 * while concurrently shrinking the corresponding Beer element's multiplicity on
 * the target side (see {@link Conflicts#testDeleteDeleteConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_BEERS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createBeersAndConflictingChanges(int nrOfBeers, int nrOfEditedBeers) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedBeers, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperBag1.createNBeers(nrOfBeers)),
								srcEdit(() -> helperBag1.deleteNBeers(nrOfEditedBeers)),
								trgEdit(() -> helperBag2.decrementBeerMultiplicity(nrOfEditedBeers))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000003ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000005ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000010ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000020ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000030ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000040ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateBeersAnd0000050ConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(NR_OF_BEERS, 50); }
}
