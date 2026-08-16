package org.benchmarx.examples.bag12bag2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
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
 * (see {@link org.benchmarx.examples.bag12bag2.testsuite.concurrent.Conflicts#testDeleteDeleteConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_BEERS = 3;

	public ScalabilityConstDeltaCSync() { super("CDCsync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperBag1.createNBeers(nrOfBeers)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfBeers,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperBag1.deleteNBeers(nrOfEditedBeers)),
						trgEdit(() -> helperBag2.decrementBeerMultiplicity(nrOfEditedBeers))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(3, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(5, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(10, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(20, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(30, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(40, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(50, NR_OF_EDITED_BEERS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(1000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(5000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(10000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(50000, NR_OF_EDITED_BEERS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(100000, NR_OF_EDITED_BEERS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(500000, NR_OF_EDITED_BEERS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000BeersAndConflictingChanges(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeersAndConflictingChanges(1000000, NR_OF_EDITED_BEERS); }
}
