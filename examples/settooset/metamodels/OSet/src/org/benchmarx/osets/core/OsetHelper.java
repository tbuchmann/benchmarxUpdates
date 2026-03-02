package org.benchmarx.osets.core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import osets.Element;
import osets.MyOrderedSet;

public class OsetHelper {
	public static abstract class Delta {
		public static class OsetElementsInversion extends Delta {
		}
	}
	
	private List<Delta> delta = new ArrayList<>();
	
	private OsetMyOrderedSetBuilder builder = null;
	private Supplier<MyOrderedSet> set;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public OsetHelper(Supplier<MyOrderedSet> set, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new OsetMyOrderedSetBuilder(set);
		this.set = set;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
		this.moveNode = moveNode;
		this.createEdge = createEdge;
	}
	
	public List<Delta> getDelta() {
		return delta;
	}
	public void clearDelta() {
		delta.clear();
	}
	
	public void setSetName() {
		set.get().setName("Alphabet");
	}
	
	public void renameAlphabetSetToABC() {
		assertTrue(set.get().getName().equals("Alphabet"));
		set.get().setName("ABC");
	}
	
	public void changeIncrementalID() {
		if ("changed".equals(set.get().getIncrementalID())) {
			set.get().setIncrementalID("changed again");
		} else {
			set.get().setIncrementalID("changed");
		}
	}
	
	public void createA() {		
		builder.next().setElementValue("A");
	}
	
	public void deleteA() {
		delete("A");
	}
	
	public void createB() {		
		builder.next().setElementValue("B");
	}
	
	public void deleteB() {
		delete("B");
	}
	
	public void createC() {		
		builder.next().setElementValue("C");
	}
	
	public void deleteC() {
		delete("C");
	}
	
	public void createD() {	
		builder.next().setElementValue("D");
	}
	
	public void deleteD() {
		delete("D");
	}
	
	public void insertABeforeC() {		
		builder.insertNewElementBefore("C").setElementValue("A");
	}
	public void insertBBeforeC() {		
		builder.insertNewElementBefore("C").setElementValue("B");
	}
	public void insertDAfterC() {	
		builder.insertNewElementAfter("C").setElementValue("D");
	}
	
	public void changeABCtoZXY() {
		getElement("A").setValue("Z");
		getElement("B").setValue("X");
		getElement("C").setValue("Y");
	}
	
	public void invert() {
		if (set.get().getElements().isEmpty()) {
			return;
		}
		
		Element first = findFirst();
		List<String> values = new ArrayList<>();
		while(first != null) {
			values.add(first.getValue());
			Element next = first.getNext();
			first.setNext(null);
			first = next;
		}
		Element last = null;
		for (String value : values) {
			Element current = getElement(value);
			current.setNext(last);
			last = current;
		}
		delta.add(new OsetHelper.Delta.OsetElementsInversion());
	}
	
	public void idleDelta() {
	}
		
	
	private Element getElement(String value) {
		Optional<Element> elementOpt = set.get().getElements().stream().filter(e -> e.getValue().equals(value)).findAny();
		
		assertTrue(elementOpt.isPresent());
		Element element = elementOpt.get();
		assertTrue(element.getValue().equals(value));
		return element;		
	}
	
	private Element findFirst() {
		assertTrue(OsetComparator.isConnected(set.get()) && !OsetComparator.isCycle(set.get()));
		
		Element first = set.get().getElements().get(0);
		while (first.getPrevious() != null) {
			first = first.getPrevious();
		}
		return first;
	}
	
	private void delete(String value) {
		Element element = getElement(value);
		if (element.getPrevious() != null) {
			element.getPrevious().setNext(element.getNext());
		}
		EcoreUtil.delete(element);
	}
}
