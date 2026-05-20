package org.benchmarx.emf.diff;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * A single discrepancy between an expected and an actual EMF object graph.
 * Implementations are immutable records.
 */
public sealed interface DiffEntry
        permits DiffEntry.MissingNode,
                DiffEntry.ExtraNode,
                DiffEntry.AttributeMismatch,
                DiffEntry.ReferenceMismatch {

    /** Human-readable path from the model root, e.g. "FamilyRegister/families/[0]" */
    String path();

    /** One-line description suitable for console output. */
    String describe();

    record MissingNode(String path, EObject expected)
            implements DiffEntry {
        public String describe() {
            return "MISSING  " + path + " — expected: " + label(expected);
        }
    }

    record ExtraNode(String path, EObject actual)
            implements DiffEntry {
        public String describe() {
            return "EXTRA    " + path + " — unexpected: " + label(actual);
        }
    }

    record AttributeMismatch(String path, EStructuralFeature feature,
                             Object expected, Object actual)
            implements DiffEntry {
        public String describe() {
            return "ATTR     " + path + "." + feature.getName()
                    + "  expected=" + quote(expected)
                    + "  actual="   + quote(actual);
        }
    }

    record ReferenceMismatch(String path, EStructuralFeature feature,
                             EObject expected, EObject actual)
            implements DiffEntry {
        public String describe() {
            return "REF      " + path + "." + feature.getName()
                    + "  expected=" + label(expected)
                    + "  actual="   + label(actual);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String label(EObject obj) {
        if (obj == null) return "<null>";
        return obj.eClass().getName() + "(" + firstStringAttr(obj) + ")";
    }

    private static String firstStringAttr(EObject obj) {
        return obj.eClass().getEAllStructuralFeatures().stream()
                .filter(f -> f instanceof org.eclipse.emf.ecore.EAttribute)
                .filter(f -> f.getEType().getInstanceClass() == String.class)
                .map(f -> String.valueOf(obj.eGet(f)))
                .findFirst()
                .orElse("?");
    }

    private static String quote(Object v) {
        return v instanceof String ? "\"" + v + "\"" : String.valueOf(v);
    }
}
