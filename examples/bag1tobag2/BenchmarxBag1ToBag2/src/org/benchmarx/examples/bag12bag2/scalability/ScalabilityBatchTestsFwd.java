package org.benchmarx.examples.bag12bag2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.bag12bag2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.bag12bag2.testsuite.Decisions;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityBatchTestsFwd extends ScalabilityTests {

	public ScalabilityBatchTestsFwd() { super("FWD_"); }

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	@AfterAll
	static void teardown() throws FileNotFoundException {
		// results saved per-run in individual tools
	}

	private void createBeers(int nrOfBeers) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfBeers, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(() -> helperBag1.createNBeers(nrOfBeers))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Beers(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeers(1000000); }
}
