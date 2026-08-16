package org.benchmarx.ecore.core;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreHelper {

	private Supplier<EPackage> packageSupplier;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;

	public EcoreHelper(Supplier<EPackage> packageSupplier, Consumer<EObject> deleteNode,
			BiConsumer<EReference, List<EObject>> deleteEdge, BiConsumer<EAttribute, List<?>> changeAttribute) {
		this.packageSupplier = packageSupplier;
		this.deleteNode = deleteNode;
		this.deleteEdge = deleteEdge;
		this.changeAttribute = changeAttribute;
	}

	public EcoreHelper(Supplier<EPackage> rootSupplier, Consumer<EObject> createSourceNode,
			BiConsumer<EReference, List<EObject>> createSourceEdge,
			BiConsumer<EAttribute, List<?>> changeSourceAttribute, Consumer<EObject> deleteSourceNode,
			BiConsumer<EObject, List<EObject>> moveSourceNode, BiConsumer<EReference, List<EObject>> deleteSourceEdge) {
		this.packageSupplier = rootSupplier;
		this.deleteNode = deleteSourceNode;
		this.deleteEdge = deleteSourceEdge;
		this.changeAttribute = changeSourceAttribute;
		this.createNode = createSourceNode;
		this.createEdge = createSourceEdge;
		this.moveNode = moveSourceNode;
	}

	// EcoreBuilder resolves packageSupplier.get() eagerly in its own constructor, so
	// it must be (re-)created on every use rather than cached as a field - otherwise
	// it would capture the model instance from before initiateSynchronisationDialogue()
	// created the real one (as happens under the scalability harness, which builds
	// helpers before the dialogue starts). EcoreBuilder holds no cross-call cursor
	// state, so recreating it per statement is safe.
	private EcoreBuilder builder() {
		return new EcoreBuilder(packageSupplier, deleteNode, deleteEdge);
	}

	public void idleDelta() {

	}

	// Creates n independent EClasses, each named prefix+i with a single "length"
	// int attribute - a scale-parameterized generalization of the "List" class from
	// createSimpleCompositeList(), used to scale the rename/rename conflict scenario
	// from Conflicts#testConcurrentRenameListLengthConflict across n classes.
	public void createNListsWithLength(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			String className = prefix + i;
			builder().clazz(className)
				.field(className, "length", "int");
		}
	}

	public void renameNListLengthAttributes(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			EClass c = (EClass) packageSupplier.get().getEClassifier(prefix + i);
			Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("length")).findAny();
			ea.ifPresent(a -> a.setName("count"));
		}
	}

	public void addNDataElementFeatures(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			builder().field(prefix + i, "extra", "boolean");
		}
	}

	public void hippocraticDelta() {
		//Delete method in List
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		EcoreUtil.delete(c.getEOperations().get(0), true);
		
		//Change some attributes from List length
		Optional<EAttribute> oa = c.getEAttributes().stream().filter(a -> a.getName().equals("length")).findAny();
		if(oa.isPresent()) {
			EAttribute a = oa.get();
			a.setChangeable(true);
			a.setVolatile(false);
			a.setDerived(false);
			a.setTransient(false);
		}
		
		builder().operation("Leaf", "isLeaf", "boolean");
		
	}
	
	public void createSimpleCompositeList() {

		builder()
		.abstractClass("Node")
		.clazz("Leaf", "Node")
		.clazz("DataNode", "Node")
			.field("DataNode", "data", "int")
			.reference("DataNode", "follower", "Node")
		.operation("Node", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("DataNode", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("Leaf", "addLast", "Node")
			.param("newNode", "DataNode")
			
		.clazz("List")
			.reference("List", "start", "Node")
			.field("List", "length", "int")
			.operation("List", "add", "boolean")
				.param("newNode","DataNode");
		EClass l = (EClass) packageSupplier.get().getEClassifier("List");
		EAttribute length = (EAttribute) l.getEStructuralFeature("length");
		length.setVolatile(true);
		length.setChangeable(false);
		length.setDerived(true);
		length.setTransient(true);
	}
	
	public void createDataAttribute() {
		
		builder().field("DataNode", "data", "int");
	}
	
	public void createDataNode() {
		
		builder().clazz("DataNode", "Node")
			.field("DataNode", "data", "int")
			.reference("DataNode", "follower", "Node")
			.operation("DataNode", "addLast", "Node")
				.param("newNode", "DataNode");
		EClass l = (EClass) packageSupplier.get().getEClassifier("List");
		EOperation o = l.getEOperations().get(0);
		EParameter param = o.getEParameters().get(0);
		param.setEType(packageSupplier.get().getEClassifier("DataNode"));
		
		l = (EClass) packageSupplier.get().getEClassifier("Node");
		o = l.getEOperations().get(0);
		param = o.getEParameters().get(0);
		param.setEType(packageSupplier.get().getEClassifier("DataNode"));
		
		l = (EClass) packageSupplier.get().getEClassifier("Leaf");
		o = l.getEOperations().get(0);
		param = o.getEParameters().get(0);
		param.setEType(packageSupplier.get().getEClassifier("DataNode"));
	}
	
	public void createMethods() {

		builder()
		.operation("Node", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("DataNode", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("Leaf", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("List", "add", "boolean")
			.param("newNode","DataElement");
	}
	
	public void createMethodsSimple() {

		builder()
		.operation("Node", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("DataNode", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("Leaf", "addLast", "Node")
			.param("newNode", "DataNode")
		.operation("List", "add", "boolean")
			.param("newNode","DataNode");
	}
	
	public void addDataElementFeature() {
		//Precondition: createSimpleCompositeList
		
		EClass l = (EClass) packageSupplier.get().getEClassifier("DataNode");
		EcoreUtil.delete(l.getEStructuralFeature("data"), true);
		builder().reference("Node", "startOf", "List");
		l = (EClass) packageSupplier.get().getEClassifier("Node");
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		EReference r = (EReference) l.getEStructuralFeature("startOf");
		r.setEOpposite((EReference) c.getEStructuralFeature("start"));
		((EReference) c.getEStructuralFeature("start")).setEOpposite(r);
		builder().iface("DataElement")
			.clazz("Pair", "DataElement")
			.clazz("Value")
				.reference("Value", "pair", "Pair")
			.clazz("Key")
				.multiField("Key", "keyValues", "String")
			.multiReference("Pair", "values", "Value")
			.reference("Pair", "key", "Key");
		builder().reference("DataNode", "data", "DataElement");
		
		
		c = (EClass) packageSupplier.get().getEClassifier("Pair");
		l = (EClass) packageSupplier.get().getEClassifier("Value");
		
		//Set Pair-Values references as containment and opposite.
		r = (EReference) l.getEStructuralFeature("pair");
		r.setEOpposite((EReference) c.getEStructuralFeature("values"));
		r = (EReference) c.getEStructuralFeature("values");
		r.setEOpposite((EReference) l.getEStructuralFeature("pair"));
		r.setContainment(true);

		//Set key reference as containment.
		r = (EReference) c.getEStructuralFeature("key");
		r.setContainment(true);
	}
	
	public void changeListAddParameter() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		EOperation o = c.getEOperations().get(0);
		o.getEParameters().clear();
		EClassifier t = packageSupplier.get().getEClassifier("DataElement");
		EParameter e = EcoreFactory.eINSTANCE.createEParameter();
		e.setName("newElement");
		e.setEType(t);
		o.getEParameters().add(e);
	}
	
	public void changeBackListAddParameter() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		EOperation o = c.getEOperations().get(0);
		o.getEParameters().clear();
		EClassifier t = packageSupplier.get().getEClassifier("DataNode");
		EParameter e = EcoreFactory.eINSTANCE.createEParameter();
		e.setName("newNode");
		e.setEType(t);
		o.getEParameters().add(e);
	}
	
	public void changePackageName() {
		
		builder().name("CompositeList");
	}
	
	public void changeGeneralizationDataElement() {
		EClass pair = (EClass) packageSupplier.get().getEClassifier("Pair");
		EClass key = (EClass) packageSupplier.get().getEClassifier("Key");
		key.getESuperTypes().addAll(pair.getESuperTypes());
		pair.getESuperTypes().clear();
	}
	
	public void changeListLengthAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("length")).findAny();
		if(ea.isPresent()) {
			EAttribute length = ea.get();
			length.setVolatile(true);
			length.setChangeable(false);
			length.setDerived(true);
			length.setTransient(true);
		}
	}
	
	public void deleteListLengthAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("length")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deleteKeyKeyValuesAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("Key");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("keyValues")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deleteNodeStartOfReference() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("Node");
		Optional<EReference> ea = c.getEReferences().stream().filter(a -> a.getName().equals("startOf")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deletePairReferences() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("Pair");
		Optional<EReference> ea = c.getEReferences().stream().filter(a -> a.getName().equals("key")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
		ea = c.getEReferences().stream().filter(a -> a.getName().equals("values")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get().getEOpposite(), true);
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deleteDataNodeDataReference() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("DataNode");
		Optional<EReference> ea = c.getEReferences().stream().filter(a -> a.getName().equals("data")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deleteDataElementFeature() {
		EcoreUtil.delete(packageSupplier.get().getEClassifier("Key"), true);
		EcoreUtil.delete(packageSupplier.get().getEClassifier("Value"), true);
		EcoreUtil.delete(packageSupplier.get().getEClassifier("Pair"), true);
		EcoreUtil.delete(packageSupplier.get().getEClassifier("DataElement"), true);
	}
	
	public void deleteDataAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("DataNode");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("data")).findAny();
		if(ea.isPresent()) {
			EcoreUtil.delete(ea.get(), true);
		}
	}
	
	public void deleteDataNode() {
		EcoreUtil.delete(packageSupplier.get().getEClassifier("DataNode"), true);
	}
	
	public void moveReferencePair() {
		EClass pair = (EClass) packageSupplier.get().getEClassifier("Pair");
		EClass value = (EClass) packageSupplier.get().getEClassifier("Value");
		EClass key = (EClass)packageSupplier.get().getEClassifier("Key");
		EReference r = value.getEReferences().get(0);
		r.getEOpposite().setEOpposite(null);
		key.getEStructuralFeatures().add(r);
		EReference newOp = (EReference)pair.getEStructuralFeature("key");
		r.setEOpposite(newOp);
		newOp.setEOpposite(r);
	}
	
	public void moveAttributeLengthAndRename() {
		EClass value = (EClass) packageSupplier.get().getEClassifier("Value");
		EClass list = (EClass) packageSupplier.get().getEClassifier("List");
		EAttribute att = list.getEAttributes().get(0);
		att.setName("value");
		value.getEStructuralFeatures().add(att);
	}
	
	public void renameListClass() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		c.setName("Queue");
	}

	public void renameListLengthAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("List");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("length")).findAny();
		if(ea.isPresent()) {
			ea.get().setName("count");
		}
	}
	
	public void renamePackage() {
		
		builder().name("CompositeQueue");
	}
	
	public void renameDataNodeDataReference() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("DataNode");
		Optional<EReference> ea = c.getEReferences().stream().filter(a -> a.getName().equals("data")).findAny();
		if(ea.isPresent()) 
			ea.get().setName("savedInformation");
	}
	
	public void renameValuesAttribute() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("Key");
		Optional<EAttribute> ea = c.getEAttributes().stream().filter(a -> a.getName().equals("keyValues")).findAny();
		if(ea.isPresent()) {
			ea.get().setName("keys");
		}
	}
	
	public void setDataElementAsInterface() {
		EClass c = (EClass) packageSupplier.get().getEClassifier("DataElement");
		c.setInterface(true);
	}
}
