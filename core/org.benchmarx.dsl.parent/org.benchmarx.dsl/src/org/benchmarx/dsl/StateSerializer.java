package org.benchmarx.dsl;

import org.benchmarx.dsl.benchmarxDSL.AttrAssign;
import org.benchmarx.dsl.benchmarxDSL.AttrValue;
import org.benchmarx.dsl.benchmarxDSL.BoolValue;
import org.benchmarx.dsl.benchmarxDSL.ChildAssign;
import org.benchmarx.dsl.benchmarxDSL.IntValue;
import org.benchmarx.dsl.benchmarxDSL.ObjectNode;
import org.benchmarx.dsl.benchmarxDSL.StateDecl;
import org.benchmarx.dsl.benchmarxDSL.StringValue;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link StateDecl} AST node into an EMF XMI file.
 *
 * Strategy:
 *  1. Load the referenced .ecore metamodel into a local ResourceSet.
 *  2. Walk the ObjectNode tree recursively, creating live EObject instances.
 *  3. Set attribute values and wire references.
 *  4. Save the root object to an XMI Resource at outputDir/<stateName>.xmi.
 */
public final class StateSerializer {

	private final ResourceSet resourceSet;

	public StateSerializer() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("xmi",   new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("ecore", new XMIResourceFactoryImpl());
	}

	/**
	 * Serialize {@code state} to an XMI string (used by the generator).
	 *
	 * @param state      the AST node to serialize
	 * @param ecorePath  absolute path to the referenced .ecore file
	 * @return XMI content as a string
	 */
	public String serializeToString(StateDecl state, Path ecorePath)
			throws IOException {
		EPackage ePackage = loadEPackage(ecorePath);
		EObject root = buildEObject(state.getRoot(), ePackage);

		URI uri = URI.createURI("__inmemory__/" + state.getName() + ".xmi");
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(root);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, Map.of());
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Serialize {@code state} to an XMI file inside {@code outputDir}.
	 *
	 * @param state      the AST node to serialize
	 * @param ecorePath  absolute path to the referenced .ecore file
	 * @param outputDir  directory where the .xmi will be written
	 */
	public void serialize(StateDecl state, Path ecorePath, Path outputDir)
			throws IOException {
		EPackage ePackage = loadEPackage(ecorePath);
		EObject root = buildEObject(state.getRoot(), ePackage);

		URI uri = URI.createFileURI(
				outputDir.resolve(state.getName() + ".xmi").toString());
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(root);
		resource.save(Map.of());
	}

	private EPackage loadEPackage(Path ecorePath) {
		URI uri = URI.createFileURI(ecorePath.toString());
		Resource r = resourceSet.getResource(uri, true);
		return (EPackage) r.getContents().get(0);
	}

	private EObject buildEObject(ObjectNode node, EPackage pkg) {
		EClass eClass = (EClass) pkg.getEClassifier(node.getEClass());
		if (eClass == null)
			throw new IllegalArgumentException("Unknown EClass: " + node.getEClass());

		EObject obj = EcoreUtil.create(eClass);

		for (AttrAssign attr : node.getAttrs()) {
			EStructuralFeature f = eClass.getEStructuralFeature(attr.getName());
			if (f == null)
				throw new IllegalArgumentException(
						"Unknown feature " + attr.getName() + " on " + eClass.getName());
			obj.eSet(f, coerce(attr.getValue(), f));
		}

		for (ChildAssign child : node.getChildren()) {
			EStructuralFeature f = eClass.getEStructuralFeature(child.getFeature());
			if (f == null)
				throw new IllegalArgumentException(
						"Unknown reference " + child.getFeature() + " on " + eClass.getName());
			if (f.isMany()) {
				@SuppressWarnings("unchecked")
				var list = (java.util.List<EObject>) obj.eGet(f);
				for (ObjectNode childNode : child.getChildren())
					list.add(buildEObject(childNode, pkg));
			} else {
				obj.eSet(f, buildEObject(child.getChildren().get(0), pkg));
			}
		}
		return obj;
	}

	private Object coerce(AttrValue value, EStructuralFeature feature) {
		Class<?> type = feature.getEType().getInstanceClass();
		if (value instanceof StringValue sv) return sv.getValue();
		if (value instanceof IntValue iv) {
			if (type == int.class || type == Integer.class) return iv.getValue();
			if (type == long.class || type == Long.class)   return (long) iv.getValue();
		}
		if (value instanceof BoolValue bv) return "true".equals(bv.getValue());
		throw new IllegalArgumentException("Cannot coerce " + value + " to " + type);
	}
}
