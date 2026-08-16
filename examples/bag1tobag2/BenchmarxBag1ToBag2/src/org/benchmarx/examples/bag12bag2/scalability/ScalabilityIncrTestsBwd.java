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
public class ScalabilityIncrTestsBwd extends ScalabilityTests {

	public ScalabilityIncrTestsBwd() { super("INCR_BWD_"); }

	public static Collection<BXTool<bags1.MyBag, bags2.MyBag, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() { initResults(); }

	@AfterAll
	static void teardown() throws FileNotFoundException { }

	private void incrementBeerMultiplicity(int multiplicity) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareTargetEditAfterSetUp(trgEdit(() -> helperBag2.createBeer(multiplicity)));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, multiplicity,
						timer.measureTargetEditAfterSetUpInS(
						trgEdit(helperBag2::incrementBeerMultiplicity)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Beer(BXTool<bags1.MyBag, bags2.MyBag, Decisions> tool) { this.tool = tool; initialise(); incrementBeerMultiplicity(1000000); }
}
