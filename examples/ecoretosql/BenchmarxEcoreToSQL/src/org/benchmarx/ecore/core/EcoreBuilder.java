package org.benchmarx.ecore.core;

import java.util.List;
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
import org.eclipse.emf.ecore.EcorePackage;

public class EcoreBuilder {

	private final EPackage pck;
	private final EcoreFactory f = EcoreFactory.eINSTANCE;
	private EOperation op;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;

	// EPackage.getEClassifier(String) is a plain O(n) linear scan (dynamically-created
	// EPackages have no name index), so every clazz()/field()/reference()/... lookup by
	// name costs O(current classifier count) - turning "create n classes" into O(n^2).
	// EcoreHelper deliberately constructs a *fresh* EcoreBuilder for every statement (see
	// its builder() method), so a cache on an instance field would never survive across
	// the very loop it needs to speed up. Keyed by EPackage identity instead, so it
	// naturally persists across builder() calls for the same package. WeakHashMap so
	// packages from finished tests can still be collected.
	private static final java.util.Map<EPackage, java.util.Map<String, EClassifier>> classifierCachesByPackage =
			new java.util.WeakHashMap<>();

	private java.util.Map<String, EClassifier> classifierCache() {
		return classifierCachesByPackage.computeIfAbsent(pck, p -> new java.util.HashMap<>());
	}

	// Self-healing: a cache hit is trusted only if the name still matches (guards against
	// staleness from a rename that bypassed this builder); a miss falls back to the
	// original O(n) scan and repopulates the cache, so behaviour is identical to calling
	// pck.getEClassifier(name) directly - just O(1) amortized for names this builder
	// itself already resolved or created.
	private EClassifier findClassifier(String name) {
		java.util.Map<String, EClassifier> cache = classifierCache();
		EClassifier cached = cache.get(name);
		if (cached != null && name.equals(cached.getName())) return cached;
		EClassifier found = pck.getEClassifier(name);
		if (found != null) cache.put(name, found);
		return found;
	}

	public EcoreBuilder(String name) {
		pck = f.createEPackage();
		pck.setName(name);
		pck.setNsPrefix(name);
		pck.setNsURI(name);
	}
	
	public EcoreBuilder(EPackage _pack) {
		pck = _pack;
	}
	
	public EcoreBuilder(Supplier<EPackage> packageSupplier, Consumer<EObject> deleteNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		pck = packageSupplier.get();
		this.createNode = deleteNode;
		this.createEdge = deleteEdge;
	}

	public EcoreBuilder name(String name) {
		pck.setName(name);
		pck.setNsPrefix(name);
		pck.setNsURI(name);
		return this;
	}
	
	public EcoreBuilder clazz(String name, String... superclasses) {
		EClass c = f.createEClass();
		c.setName(name);
		for (String s : superclasses) {
			EClassifier sup = findClassifier(s);
			if (sup instanceof EClass)
				c.getESuperTypes().add((EClass)sup);
		}
		pck.getEClassifiers().add(c);
		classifierCache().put(name, c);
		return this;
	}

	public EcoreBuilder abstractClass(String name, String... superclasses) {
		EClass c = f.createEClass();
		c.setName(name);
		c.setAbstract(true);
		for (String s : superclasses) {
			EClassifier sup = findClassifier(s);
			if (sup instanceof EClass)
				c.getESuperTypes().add((EClass)sup);
		}
		pck.getEClassifiers().add(c);
		classifierCache().put(name, c);
		return this;
	}

	public EcoreBuilder iface(String name, String... superclasses) {
		EClass c = f.createEClass();
		c.setName(name);
		c.setInterface(true);
		c.setAbstract(true);
		for (String s : superclasses) {
			EClassifier sup = findClassifier(s);
			if (sup instanceof EClass)
				c.getESuperTypes().add((EClass)sup);
		}
		pck.getEClassifiers().add(c);
		classifierCache().put(name, c);
		return this;
	}
	
	public EcoreBuilder field(String parentClass, String name, String type) {
		EClassifier parent = findClassifier(parentClass);
		if (parent instanceof EClass) {
			EAttribute att = f.createEAttribute();
			att.setName(name);
			
			if (type.equals("int"))
				att.setEType(EcorePackage.eINSTANCE.getEInt());
			else if (type.equals("boolean"))
				att.setEType(EcorePackage.eINSTANCE.getEBoolean());
			else if (type.equals("String"))
				att.setEType(EcorePackage.eINSTANCE.getEString());
			if (type.equals("double"))
				att.setEType(EcorePackage.eINSTANCE.getEDouble());
			
			att.setUpperBound(1);
			((EClass)parent).getEStructuralFeatures().add(att);
		}
		
		return this;
	}
	
	public EcoreBuilder multiField(String parentClass, String name, String type) {
		EClassifier parent = findClassifier(parentClass);
		if (parent instanceof EClass) {
			EAttribute att = f.createEAttribute();
			att.setName(name);
			
			if (type.equals("int"))
				att.setEType(EcorePackage.eINSTANCE.getEInt());
			else if (type.equals("boolean"))
				att.setEType(EcorePackage.eINSTANCE.getEBoolean());
			else if (type.equals("String"))
				att.setEType(EcorePackage.eINSTANCE.getEString());
			if (type.equals("double"))
				att.setEType(EcorePackage.eINSTANCE.getEDouble());
			
			att.setUpperBound(-1);
			((EClass)parent).getEStructuralFeatures().add(att);
		}
		
		return this;
	}
	
	public EcoreBuilder operation(String parentClass, String name, String type) {
		EClassifier parent = findClassifier(parentClass);
		EClassifier t = findClassifier(type);
		if (parent instanceof EClass) {
			op = f.createEOperation();
			op.setName(name);
			
			if (type.equals("int"))
				op.setEType(EcorePackage.eINSTANCE.getEInt());
			else if (type.equals("boolean"))
				op.setEType(EcorePackage.eINSTANCE.getEBoolean());
			else if (type.equals("String"))
				op.setEType(EcorePackage.eINSTANCE.getEString());
			else if (type.equals("double"))
				op.setEType(EcorePackage.eINSTANCE.getEDouble());
			else
				op.setEType(t);
			
			op.setUpperBound(1);
			((EClass)parent).getEOperations().add(op);
		}
		
		return this;
	}
	
	public EcoreBuilder multiOperation(String parentClass, String name, String type) {
		EClassifier parent = findClassifier(parentClass);
		EClassifier t = findClassifier(type);
		if (parent instanceof EClass) {
			op = f.createEOperation();
			op.setName(name);
			
			if (type.equals("int"))
				op.setEType(EcorePackage.eINSTANCE.getEInt());
			else if (type.equals("boolean"))
				op.setEType(EcorePackage.eINSTANCE.getEBoolean());
			else if (type.equals("String"))
				op.setEType(EcorePackage.eINSTANCE.getEString());
			else if (type.equals("double"))
				op.setEType(EcorePackage.eINSTANCE.getEDouble());
			else
				op.setEType(t);
			
			op.setUpperBound(-1);
			((EClass)parent).getEOperations().add(op);
		}
		
		return this;
	}
	
	public EcoreBuilder param(String name, String type) {
		EClassifier t = findClassifier(type);
		EParameter param = f.createEParameter();
		param.setName(name);
			
		if (type.equals("int"))
			param.setEType(EcorePackage.eINSTANCE.getEInt());
		else if (type.equals("boolean"))
			param.setEType(EcorePackage.eINSTANCE.getEBoolean());
		else if (type.equals("String"))
			param.setEType(EcorePackage.eINSTANCE.getEString());
		else if (type.equals("double"))
			param.setEType(EcorePackage.eINSTANCE.getEDouble());
		else if(t != null)
			param.setEType(t);
		else
			return this;
			
		param.setUpperBound(1);
		op.getEParameters().add(param);

		return this;
	}
	
	public EcoreBuilder multiParam(String name, String type) {
		EClassifier t = findClassifier(type);
		EParameter param = f.createEParameter();
		param.setName(name);
			
		if (type.equals("int"))
			param.setEType(EcorePackage.eINSTANCE.getEInt());
		else if (type.equals("boolean"))
			param.setEType(EcorePackage.eINSTANCE.getEBoolean());
		else if (type.equals("String"))
			param.setEType(EcorePackage.eINSTANCE.getEString());
		else if (type.equals("double"))
			param.setEType(EcorePackage.eINSTANCE.getEDouble());
		else if(t != null)
			param.setEType(t);
		else
			return this;
			
		param.setUpperBound(-1);
		op.getEParameters().add(param);

		return this;
	}
	
	public EcoreBuilder reference(String parentClass, String name, String targetClass) {
		EClassifier parent = findClassifier(parentClass);
		EClassifier target = findClassifier(targetClass);
		if (parent instanceof EClass && target != null && target instanceof EClass) {
			EReference ref = f.createEReference();
			ref.setName(name);
			ref.setEType(target);
			
			ref.setUpperBound(1);
			((EClass)parent).getEStructuralFeatures().add(ref);
		}
		
		return this;
	}
	
	public EcoreBuilder multiReference(String parentClass, String name, String targetClass) {
		EClassifier parent = findClassifier(parentClass);
		EClassifier target = findClassifier(targetClass);
		if (parent instanceof EClass && target != null && target instanceof EClass) {
			EReference ref = f.createEReference();
			ref.setName(name);
			ref.setEType(target);
			
			ref.setUpperBound(-1);
			((EClass)parent).getEStructuralFeatures().add(ref);
		}
		
		return this;
	}
	
	public EPackage end() {
		return pck;
	}
}
