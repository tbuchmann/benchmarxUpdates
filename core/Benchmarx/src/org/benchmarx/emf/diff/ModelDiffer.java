package org.benchmarx.emf.diff;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class ModelDiffer {

    private ModelDiffer() {}

    /**
     * Produces a full diff between two EMF model roots.
     * Never throws; always returns a {@link ModelDiff}.
     */
    public static ModelDiff diff(EObject expected, EObject actual) {
        List<DiffEntry> entries = new ArrayList<>();
        compare(expected, actual, new ArrayDeque<>(), entries);
        return new ModelDiff(entries);
    }

    // ── recursive core ───────────────────────────────────────────────────────

    private static void compare(EObject expected, EObject actual,
                                Deque<String> path, List<DiffEntry> out) {
        if (expected == null && actual == null) return;

        String currentPath = pathString(path);

        if (expected == null) {
            out.add(new DiffEntry.ExtraNode(currentPath, actual));
            return;
        }
        if (actual == null) {
            out.add(new DiffEntry.MissingNode(currentPath, expected));
            return;
        }
        if (!expected.eClass().equals(actual.eClass())) {
            out.add(new DiffEntry.MissingNode(currentPath, expected));
            out.add(new DiffEntry.ExtraNode(currentPath, actual));
            return;
        }

        for (EAttribute attr : expected.eClass().getEAllAttributes()) {
            Object expVal = expected.eGet(attr);
            Object actVal = actual.eGet(attr);
            if (!Objects.equals(expVal, actVal)) {
                out.add(new DiffEntry.AttributeMismatch(currentPath, attr, expVal, actVal));
            }
        }

        for (EReference ref : expected.eClass().getEAllReferences()) {
            if (ref.isTransient() || ref.isDerived()) continue;

            path.push(ref.getName());
            if (ref.isMany()) {
                @SuppressWarnings("unchecked")
                List<EObject> expList = (List<EObject>) expected.eGet(ref);
                @SuppressWarnings("unchecked")
                List<EObject> actList = (List<EObject>) actual.eGet(ref);
                compareList(expList, actList, path, out);
            } else {
                EObject expChild = (EObject) expected.eGet(ref);
                EObject actChild = (EObject) actual.eGet(ref);
                compare(expChild, actChild, path, out);
            }
            path.pop();
        }
    }

    private static void compareList(List<EObject> expected, List<EObject> actual,
                                    Deque<String> path, List<DiffEntry> out) {
        int max = Math.max(expected.size(), actual.size());
        for (int i = 0; i < max; i++) {
            path.push("[" + i + "]");
            EObject exp = i < expected.size() ? expected.get(i) : null;
            EObject act = i < actual.size()   ? actual.get(i)   : null;
            compare(exp, act, path, out);
            path.pop();
        }
    }

    // Deque is used as a stack (push/pop), so the deepest element is at the front.
    // Reverse before joining to produce a root-to-leaf path string.
    private static String pathString(Deque<String> path) {
        if (path.isEmpty()) return "/";
        List<String> parts = new ArrayList<>(path);
        Collections.reverse(parts);
        return String.join("/", parts);
    }
}
