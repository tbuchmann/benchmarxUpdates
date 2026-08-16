package org.benchmarx.bags1.core;



import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import bags1.Element;
import bags1.MyBag;

public class Bag1Helper {

	private Bag1MyBagBuilder builder;
	private Supplier<MyBag> bag;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public Bag1Helper(Supplier<MyBag> bag, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new Bag1MyBagBuilder(bag);//, createNode, createEdge);
		this.bag = bag;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
		this.moveNode = moveNode;
		this.createEdge = createEdge;
	}
	
	
	public void createOneBeer() {
		
		builder.addElement().setValue("Beer");
	}
	
	public void createFiveBeers() {
		
		builder.addElement().setValue("Beer")
				.addElement().setValue("Beer")
				.addElement().setValue("Beer")
				.addElement().setValue("Beer")
				.addElement().setValue("Beer");
	}
	
	public void createBeerGlass() {

		builder.addElement().setValue("Beer Glass");
	}

	public void createNBeers(int n) {
		for (int i = 0; i < n; i++) {
			builder.addElement().setValue("Beer");
		}
	}

	public void createNBeerGlasses(int n) {
		for (int i = 0; i < n; i++) {
			builder.addElement().setValue("Beer Glass");
		}
	}

	public void deleteBeer() {
		EcoreUtil.delete(getElement("Beer"), true);
	}

	public void deleteNBeers(int n) {
		for (int i = 0; i < n; i++) {
			deleteBeer();
		}
	}
	
	public void deleteBeerGlass() {
		EcoreUtil.delete(getElement("Beer Glass"), true);
	}
	
	public void changeOneBeerToEmptyBottle() {
		getElement("Beer").setValue("Empty Bottle");
	}
	
	public void changeAllBeerToEmptyBottles() {
		getElement("Beer").setValue("Empty Bottle");
		getElement("Beer").setValue("Empty Bottle");
		getElement("Beer").setValue("Empty Bottle");
		getElement("Beer").setValue("Empty Bottle");
		getElement("Beer").setValue("Empty Bottle");
	}
	
	public void changeIncrementalID() {
		getElement("Empty Bottle").setIncrementalID("incrIDTestValue");
	}
	
	public void idleDelta() {
	}
	
	private Element getElement(String value) {
		Optional<Element> elementOpt = bag.get().getElements().stream().filter(e -> ((Element) e).getValue().equals(value)).findAny();
		
		if (!elementOpt.isPresent()) throw new IllegalStateException("Element with value not found: " + value);
		Element element = elementOpt.get();
		if (!element.getValue().equals(value)) throw new AssertionError("Element value mismatch: expected " + value + " but was " + element.getValue());
		return element;		
	}
}
