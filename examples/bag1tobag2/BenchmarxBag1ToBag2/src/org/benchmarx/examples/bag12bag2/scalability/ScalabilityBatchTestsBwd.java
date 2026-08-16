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
public class ScalabilityBatchTestsBwd extends ScalabilityTests {

	public ScalabilityBatchTestsBwd() { super("BWD_"); }

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { /* results saved per test in external runners */ }

	private void createBeer(int multiplicity) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, multiplicity,
						timer.timeTargetEditFromScratchInS(trgEdit(() -> helperBag2.createBeer(multiplicity))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(100); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); createBeer(1000000); }
}
