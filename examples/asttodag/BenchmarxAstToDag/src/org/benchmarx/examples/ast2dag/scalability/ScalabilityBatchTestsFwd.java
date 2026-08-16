package org.benchmarx.examples.ast2dag.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
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

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
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

	private void createBestDigits(int nrOfDigits) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfDigits, //
						timer.timeSourceEditFromScratchInS(
								srcEdit(() -> helperAst.createNBestDigits(nrOfDigits))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(3); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(5); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(10); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(50); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(100); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000300Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(300); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(1000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(5000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(10000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(50000); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(100000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(500000); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000Digits(BXTool<ast.Model, dag.Model, Decisions> tool) { this.tool = tool; initialise(); createBestDigits(1000000); }
}
