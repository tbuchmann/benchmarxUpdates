package org.benchmarx.comparator;

import org.benchmarx.emf.diff.ModelDiff;
import org.benchmarx.emf.diff.ModelDiffer;
import org.benchmarx.emf.diff.TextDiffRenderer;
import org.eclipse.emf.ecore.EObject;
import org.opentest4j.AssertionFailedError;

import java.util.function.BiConsumer;

/**
 * Drop-in replacement for the raw {@code BiConsumer<M, M>} previously used
 * as a comparator in {@link org.benchmarx.emf.BXToolForEMF}.
 *
 * <p>Subclasses implement only {@link #modelName()}; all diff and assertion
 * logic is handled here.
 */
public abstract class BenchmarxComparator<M extends EObject>
        implements BiConsumer<M, M> {

    private final TextDiffRenderer renderer = new TextDiffRenderer();

    /** A short label for this model type, e.g. {@code "FamilyRegister"}. */
    protected abstract String modelName();

    @Override
    public final void accept(M expected, M actual) {
        ModelDiff diff = ModelDiffer.diff(expected, actual);
        if (!diff.isEmpty()) {
            String report = renderer.render(diff);
            throw new AssertionFailedError(
                    "Mismatch in [" + modelName() + "]:" + report,
                    expected, actual
            );
        }
    }
}
