package org.benchmarx.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.benchmarx.BXTool;
import org.benchmarx.edit.IEdit;

/**
 * Helper class used for running runtime measurements for a {@link BXTool}. See
 * {@link BXTool} for a documentation of all type parameters.
 *
 * @author anthony anjorin
 *
 * @param <S>
 * @param <T>
 * @param <D>
 */
public class BXToolTimer<S, T, D> {

	protected final BXTool<S, T, D> tool;
	protected final int REPEAT;

	/**
	 * @param tool   {@link BXTool} to be timed.
	 * @param repeat How often to repeat each timed propagation.
	 */
	public BXToolTimer(BXTool<S, T, D> tool, int repeat) {
		this.tool = tool;
		this.REPEAT = repeat;
	}

	private long median(Supplier<Long> measurement) {
		List<Long> measurements = Stream.generate(measurement).limit(REPEAT).sorted().collect(Collectors.toList());

		return measurements.get(REPEAT / 2);
	}

	protected long timeAction(Runnable action) {
		long tic = System.currentTimeMillis();
		action.run();
		long toc = System.currentTimeMillis();

		return toc - tic;
	}

	/**
	 * Eagerly resolves {@code edit} (running whatever test-harness helper code builds
	 * it, e.g. constructing N model elements) and wraps the already-built result in a
	 * trivial {@link Supplier} that just hands it back. Every {@code BXTool} calls
	 * {@code edit.get()} once as the very first step of its
	 * {@code performAndPropagateXEdit}/{@code performAndPropagateEdit} implementation,
	 * before doing the actual transformation - so materializing outside the timed
	 * region and re-supplying the cached result lets us time only the tool's real
	 * propagation/transformation work, not the cost of building the edit itself.
	 */
	private <E> Supplier<E> materialize(Supplier<E> edit) {
		E materialized = edit.get();
		return () -> materialized;
	}

	/**
	 * Perform runtime measurements from scratch (i.e., a batch transformation).
	 *
	 * @param edit The source edit to be propagated and timed.
	 * @return The median of propagating edit REPEAT times
	 */
	public long timeSourceEditFromScratchInMS(Supplier<IEdit<S>> edit) {
		return median(() -> {
			tool.initiateSynchronisationDialogue();
			tool.noPrecondition();
			Supplier<IEdit<S>> preBuilt = materialize(edit);
			var time = timeAction(() -> tool.performAndPropagateSourceEdit(preBuilt));
			tool.terminateSynchronisationDialogue();
			return time;
		});
	}

	/**
	 * See {@link #timeSourceEditFromScratchInMS(Consumer)}
	 */
	public double timeSourceEditFromScratchInS(Supplier<IEdit<S>> edit) {
		return timeSourceEditFromScratchInMS(edit) / 1000.0;
	}

	/**
	 * See {@link #timeSourceEditFromScratchInMS(Consumer)}
	 */
	public long timeTargetEditFromScratchInMS(Supplier<IEdit<T>> edit) {
		return median(() -> {
			tool.initiateSynchronisationDialogue();
			tool.noPrecondition();
			Supplier<IEdit<T>> preBuilt = materialize(edit);
			var time = timeAction(() -> tool.performAndPropagateTargetEdit(preBuilt));
			tool.terminateSynchronisationDialogue();
			return time;
		});
	}

	/**
	 * See {@link #timeSourceEditFromScratchInMS(Consumer)}
	 */
	public double timeTargetEditFromScratchInS(Supplier<IEdit<T>> edit) {
		return timeTargetEditFromScratchInMS(edit) / 1000.0;
	}

	/**
	 * Perform runtime measurements after an initial setup (i.e., for incremental
	 * transformations)
	 *
	 * @param setup The initial source edit that is propagated to establish the
	 *              starting point for the measurement. This is not measured.
	 * @param edit  The source edit to be propagated and timed.
	 * @return The median of propagating edit REPEAT times (each time executed after
	 *         a fresh setup).
	 */
	public long timeSourceEditAfterSetUpInMS(Supplier<IEdit<S>> setup, Supplier<IEdit<S>> edit) {
		return median(() -> {
			tool.initiateSynchronisationDialogue();
			tool.noPrecondition();
			tool.performAndPropagateSourceEdit(setup);
			Supplier<IEdit<S>> preBuilt = materialize(edit);
			var time = timeAction(() -> tool.performAndPropagateSourceEdit(preBuilt));
			tool.terminateSynchronisationDialogue();
			return time;
		});
	}

	/**
	 * See {@link #timeSourceEditAfterSetUpInMS(Consumer, Consumer)}
	 */
	public long timeTargetEditAfterSetUpInMS(Supplier<IEdit<T>> setup, Supplier<IEdit<T>> edit) {
		return median(() -> {
			tool.initiateSynchronisationDialogue();
			tool.noPrecondition();
			tool.performAndPropagateTargetEdit(setup);
			Supplier<IEdit<T>> preBuilt = materialize(edit);
			var time = timeAction(() -> tool.performAndPropagateTargetEdit(preBuilt));
			tool.terminateSynchronisationDialogue();
			return time;
		});
	}

	/**
	 * See {@link #timeSourceEditAfterSetUpInMS(Consumer, Consumer)}
	 */
	public double timeTargetEditAfterSetUpInS(Supplier<IEdit<T>> setup, Supplier<IEdit<T>> edit) {
		return timeTargetEditAfterSetUpInMS(setup, edit) / 1000.0;
	}

	/**
	 * See {@link #timeSourceEditAfterSetUpInMS(Consumer, Consumer)}
	 */
	public double timeSourceEditAfterSetUpInS(Supplier<IEdit<S>> setup, Supplier<IEdit<S>> edit) {
		return timeSourceEditAfterSetUpInMS(setup, edit) / 1000.0;
	}

	/**
	 * Perform runtime measurements after an initial setup (i.e., for incremental
	 * transformations)
	 *
	 * @param setup The initial source edit that is propagated to establish the
	 *              starting point for the measurement. This is not measured.
	 * @param sourceEdit  The source edit to be propagated.
	 * @param targetEdit  The target edit to be propagated.
	 * @return The median of propagating both edits REPEAT times (each time executed after
	 *         a fresh setup).
	 */
	public long timeEditAfterSetUpInMS(Supplier<IEdit<S>> setup, Supplier<IEdit<S>> sourceEdit, Supplier<IEdit<T>> targetEdit) {
		return median(() -> {
			tool.initiateSynchronisationDialogue();
			tool.noPrecondition();
			tool.performAndPropagateSourceEdit(setup);
			Supplier<IEdit<S>> preSrc = materialize(sourceEdit);
			Supplier<IEdit<T>> preTrg = materialize(targetEdit);
			var time = timeAction(() -> tool.performAndPropagateEdit(preSrc, preTrg));
			tool.terminateSynchronisationDialogue();
			return time;
		});
	}

	/**
	 * See {@link #timeSourceEditAfterSetUpInMS(Consumer, Consumer)}
	 */
	public double timeEditAfterSetUpInS(Supplier<IEdit<S>> setup, Supplier<IEdit<S>> sourceEdit, Supplier<IEdit<T>> targetEdit) {
		return timeEditAfterSetUpInMS(setup, sourceEdit, targetEdit) / 1000.0;
	}

	/**
	 * Untimed preparation phase for an "after setup" measurement: initiates a fresh
	 * dialogue and fully propagates {@code setup}. Deliberately not wrapped in any
	 * timeout by this class - building and propagating the seed model can
	 * legitimately take longer than the single delta propagation being measured,
	 * and that setup cost is not what these tests are meant to bound. Callers
	 * should invoke this <em>outside</em> their own timeout boundary (e.g. outside
	 * {@code assertTimeoutPreemptively}), then wrap only the matching
	 * {@code measure*AfterSetUp} call in the timeout.
	 *
	 * <p>Pairs with {@link #measureSourceEditAfterSetUpInS(Supplier)}. Since this
	 * splits what {@link #timeSourceEditAfterSetUpInS} does into two calls, it
	 * does not itself support {@code REPEAT > 1} medians - callers doing so should
	 * loop prepare+measure together.</p>
	 */
	public void prepareSourceEditAfterSetUp(Supplier<IEdit<S>> setup) {
		tool.initiateSynchronisationDialogue();
		tool.noPrecondition();
		tool.performAndPropagateSourceEdit(setup);
	}

	/**
	 * See {@link #prepareSourceEditAfterSetUp(Supplier)}
	 */
	public void prepareTargetEditAfterSetUp(Supplier<IEdit<T>> setup) {
		tool.initiateSynchronisationDialogue();
		tool.noPrecondition();
		tool.performAndPropagateTargetEdit(setup);
	}

	/**
	 * Timed measurement phase, to be called after the matching
	 * {@link #prepareSourceEditAfterSetUp(Supplier)}. Materializes {@code edit}
	 * (untimed - see {@link #materialize}), times only the propagation call, then
	 * terminates the dialogue.
	 */
	public double measureSourceEditAfterSetUpInS(Supplier<IEdit<S>> edit) {
		Supplier<IEdit<S>> preBuilt = materialize(edit);
		long ms = timeAction(() -> tool.performAndPropagateSourceEdit(preBuilt));
		tool.terminateSynchronisationDialogue();
		return ms / 1000.0;
	}

	/**
	 * See {@link #measureSourceEditAfterSetUpInS(Supplier)}, to be called after
	 * {@link #prepareTargetEditAfterSetUp(Supplier)}.
	 */
	public double measureTargetEditAfterSetUpInS(Supplier<IEdit<T>> edit) {
		Supplier<IEdit<T>> preBuilt = materialize(edit);
		long ms = timeAction(() -> tool.performAndPropagateTargetEdit(preBuilt));
		tool.terminateSynchronisationDialogue();
		return ms / 1000.0;
	}

	/**
	 * See {@link #measureSourceEditAfterSetUpInS(Supplier)}, for the concurrent
	 * (both-sides) case - to be called after
	 * {@link #prepareSourceEditAfterSetUp(Supplier)}.
	 */
	public double measureEditAfterSetUpInS(Supplier<IEdit<S>> sourceEdit, Supplier<IEdit<T>> targetEdit) {
		Supplier<IEdit<S>> preSrc = materialize(sourceEdit);
		Supplier<IEdit<T>> preTrg = materialize(targetEdit);
		long ms = timeAction(() -> tool.performAndPropagateEdit(preSrc, preTrg));
		tool.terminateSynchronisationDialogue();
		return ms / 1000.0;
	}

	public String getName() {
		return tool.getName();
	}
}
