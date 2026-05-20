package org.benchmarx.emf.diff;

import java.util.Collections;
import java.util.List;

/**
 * Immutable result of comparing two EMF model roots.
 */
public final class ModelDiff {

    private final List<DiffEntry> entries;

    public ModelDiff(List<DiffEntry> entries) {
        this.entries = Collections.unmodifiableList(entries);
    }

    public List<DiffEntry> entries() { return entries; }
    public boolean isEmpty()         { return entries.isEmpty(); }
    public int size()                { return entries.size(); }

    public static ModelDiff empty() {
        return new ModelDiff(List.of());
    }
}
