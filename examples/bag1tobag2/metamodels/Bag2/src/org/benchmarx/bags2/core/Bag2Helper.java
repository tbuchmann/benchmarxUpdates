package org.benchmarx.bags2.core;



import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import bags2.Element;
import bags2.MyBag;

public class Bag2Helper {

	private Bag2MyBagBuilder builder;
	private Supplier<MyBag> bag;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public Bag2Helper(Supplier<MyBag> bag, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new Bag2MyBagBuilder(bag);//, createNode, createEdge);
		this.bag = bag;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
		this.moveNode = moveNode;
		this.createEdge = createEdge;
	}
	
	public void createOneBeer() {
		//builder = new Bag2MyBagBuilder(bag);
		builder.addElement().setMultiplicity(1).setValue("Beer");
	}
	
	public void createFourBeer() {
		//builder = new Bag2MyBagBuilder(bag);
		builder.addElement().setMultiplicity(4).setValue("Beer");
	}
	
	public void createFiveBeer() {
		//builder = new Bag2MyBagBuilder(bag);
		builder.addElement().setMultiplicity(5).setValue("Beer");
	}
	
	public void createEmptyBottle() {
		//builder = new Bag2MyBagBuilder(bag);
		builder.addElement().setMultiplicity(1).setValue("Empty Bottle");
	}
	
	public void createBeerGlass() {
		//builder = new Bag2MyBagBuilder(bag);
		builder.addElement().setMultiplicity(1).setValue("Beer Glass");
	}

	public void createBeer(int multiplicity) {
		builder.addElement().setMultiplicity(multiplicity).setValue("Beer");
	}

	public void createNEmptyBottles(int n) {
		builder.addElement().setMultiplicity(n).setValue("Empty Bottle");
	}

	public void incrementBeerMultiplicity() {
		Element beer = getElement("Beer");
		beer.setMultiplicity(beer.getMultiplicity() + 1);
	}

	public void decrementBeerMultiplicity(int n) {
		Element beer = getElement("Beer");
		int newMultiplicity = beer.getMultiplicity() - n;
		if (newMultiplicity <= 0) {
			EcoreUtil.delete(beer, true);
		} else {
			beer.setMultiplicity(newMultiplicity);
		}
	}

	public void deleteBeerGlass () {
		EcoreUtil.delete(getElement("Beer Glass"), true);
	}

	public void deleteAllBeers () {
		EcoreUtil.delete(getElement("Beer"), true);
	}
	
	public void changeEmptyBottleToBrokenBottle() {
		getElement("Empty Bottle").setValue("Broken Bottle");
	}
	
	public void changeBeerGlassToEmptyBottle() {
		getElement("Beer Glass").setValue("Empty Bottle");
	}
	
	public void changeMultiplicityOfBeer() {
		getElement("Beer").setMultiplicity(2);
	}
	
	public void changeBeerToEmptyBottle() {
		getElement("Beer").setValue("Empty Bottle");
	}
	
	public void changeMultiplicityOfBeerGlass() {
		getElement("Beer Glass").setMultiplicity(2);
	}
	
	public void changeIncrementalID() {
		getElement("Beer").setIncrementalID("incrTestValue");
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
