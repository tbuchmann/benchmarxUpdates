package org.benchmarx.emf.diff;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelDifferTest {

    private EPackage pkg;
    private EClass nodeClass;
    private EAttribute nameAttr;
    private EReference childrenRef;

    @BeforeEach
    void setUp() {
        EcoreFactory ef = EcoreFactory.eINSTANCE;

        pkg = ef.createEPackage();
        pkg.setName("test");
        pkg.setNsURI("http://test");
        pkg.setNsPrefix("test");

        nodeClass = ef.createEClass();
        nodeClass.setName("Node");
        pkg.getEClassifiers().add(nodeClass);

        nameAttr = ef.createEAttribute();
        nameAttr.setName("name");
        nameAttr.setEType(EcorePackage.Literals.ESTRING);
        nodeClass.getEStructuralFeatures().add(nameAttr);

        childrenRef = ef.createEReference();
        childrenRef.setName("children");
        childrenRef.setEType(nodeClass);
        childrenRef.setContainment(true);
        childrenRef.setUpperBound(-1);
        nodeClass.getEStructuralFeatures().add(childrenRef);
    }

    private EObject node(String name) {
        EObject obj = pkg.getEFactoryInstance().create(nodeClass);
        obj.eSet(nameAttr, name);
        return obj;
    }

    @SuppressWarnings("unchecked")
    private void addChild(EObject parent, EObject child) {
        ((org.eclipse.emf.common.util.EList<EObject>) parent.eGet(childrenRef)).add(child);
    }

    @Test
    void identical_models_produce_empty_diff() {
        EObject a = node("foo");
        EObject b = node("foo");
        assertTrue(ModelDiffer.diff(a, b).isEmpty());
    }

    @Test
    void attribute_mismatch_detected() {
        EObject expected = node("foo");
        EObject actual   = node("bar");
        ModelDiff diff = ModelDiffer.diff(expected, actual);
        assertEquals(1, diff.size());
        assertInstanceOf(DiffEntry.AttributeMismatch.class, diff.entries().get(0));
        DiffEntry.AttributeMismatch m = (DiffEntry.AttributeMismatch) diff.entries().get(0);
        assertEquals("foo", m.expected());
        assertEquals("bar", m.actual());
    }

    @Test
    void missing_child_node_detected() {
        EObject expected = node("parent");
        EObject actual   = node("parent");
        addChild(expected, node("child"));

        ModelDiff diff = ModelDiffer.diff(expected, actual);
        assertEquals(1, diff.size());
        assertInstanceOf(DiffEntry.MissingNode.class, diff.entries().get(0));
    }

    @Test
    void extra_child_node_detected() {
        EObject expected = node("parent");
        EObject actual   = node("parent");
        addChild(actual, node("extra"));

        ModelDiff diff = ModelDiffer.diff(expected, actual);
        assertEquals(1, diff.size());
        assertInstanceOf(DiffEntry.ExtraNode.class, diff.entries().get(0));
    }

    @Test
    void multiple_differences_all_collected() {
        EObject expected = node("alpha");
        EObject actual   = node("beta");
        addChild(expected, node("c1"));
        addChild(expected, node("c2"));
        // actual has no children → two MissingNode + one AttributeMismatch

        ModelDiff diff = ModelDiffer.diff(expected, actual);
        assertEquals(3, diff.size());
    }

    @Test
    void different_eclass_at_root() {
        // Create a second EClass to force an eClass mismatch at root
        EClass otherClass = EcoreFactory.eINSTANCE.createEClass();
        otherClass.setName("Other");
        pkg.getEClassifiers().add(otherClass);

        EObject expected = pkg.getEFactoryInstance().create(nodeClass);
        EObject actual   = pkg.getEFactoryInstance().create(otherClass);

        ModelDiff diff = ModelDiffer.diff(expected, actual);
        assertEquals(2, diff.size());
        assertInstanceOf(DiffEntry.MissingNode.class, diff.entries().get(0));
        assertInstanceOf(DiffEntry.ExtraNode.class,   diff.entries().get(1));
        assertEquals("/", diff.entries().get(0).path());
        assertEquals("/", diff.entries().get(1).path());
    }

    @Test
    void nested_attribute_path_is_correct() {
        EObject expectedRoot = node("root");
        EObject expectedChild = node("foo");
        addChild(expectedRoot, expectedChild);

        EObject actualRoot = node("root");
        EObject actualChild = node("bar"); // attribute differs at depth 2
        addChild(actualRoot, actualChild);

        ModelDiff diff = ModelDiffer.diff(expectedRoot, actualRoot);
        assertEquals(1, diff.size());
        DiffEntry entry = diff.entries().get(0);
        assertInstanceOf(DiffEntry.AttributeMismatch.class, entry);
        // path must contain both the reference name and the index
        String path = entry.path();
        assertTrue(path.contains("children"), "path should contain reference name: " + path);
        assertTrue(path.contains("[0]"),       "path should contain index: " + path);
    }
}
