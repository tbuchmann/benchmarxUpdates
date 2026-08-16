package org.benchmarx.examples.bag12bag2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.benchmarx.examples.bag12bag2.testsuite.concurrent.MonotonicCreating;
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
 * changes (see {@link MonotonicCreating#testConcurrentAddNonMatchingValues}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_BEERS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperBag1.createNBeers(nrOfBeers)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfBeers,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperBag1.createNBeerGlasses(nrOfEditedBeers)),
						trgEdit(() -> helperBag2.createNEmptyBottles(nrOfEditedBeers))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(3, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(5, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(10, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(20, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(30, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(40, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(50, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(60, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(70, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(80, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(90, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(100, NR_OF_EDITED_BEERS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(1000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(5000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(10000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(50000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(100000, NR_OF_EDITED_BEERS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(500000, NR_OF_EDITED_BEERS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000BeersAndConflictFreeChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictFreeChanges(1000000, NR_OF_EDITED_BEERS); }
}
