package org.benchmarx.sets.core;

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

import sets.Element;
import sets.MySet;

public class SetHelper {
	public static abstract class Delta {
		public static class SetNameChange extends Delta {
			public final String newName;

			public SetNameChange(String newName) {
				this.newName = newName;
			}
		}
		
		public static class ElementCreation extends Delta {
			public final String value;

			public ElementCreation(String value) {
				this.value = value;
			}
		}
		
		public static class ElementDeletion extends Delta {
			public final String value;
			
			public ElementDeletion(String value) {
				this.value = value;
			}
		}
		
		public static class ElementChange extends Delta {
			public final String oldValue;
			public final String newValue;
			
			public ElementChange(String oldValue, String newValue) {
				this.oldValue = oldValue;
				this.newValue = newValue;
			}
		}
	}
	
	private SetMySetBuilder builder = null;
	private List<Delta> delta = new ArrayList<>();
	
	private Supplier<MySet> set;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public SetHelper(Supplier<MySet> set, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new SetMySetBuilder(set);
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
		delta.add(new SetHelper.Delta.SetNameChange("Alphabet"));
	}
	
	public void renameAlphabetSetToABC() {
		assertTrue(set.get().getName().equals("Alphabet"));
		set.get().setName("ABC");
		delta.add(new SetHelper.Delta.SetNameChange("ABC"));
	}
	
	public void changeIncrementalID() {
		if ("changed".equals(set.get().getIncrementalID())) {
			set.get().setIncrementalID("changed again");
		} else {
			set.get().setIncrementalID("changed");
		}
	}
	
	public void createA() {		
		builder.addElement().setElementValue("A");
		delta.add(new SetHelper.Delta.ElementCreation("A"));
	}
	
	public void deleteA() {
		EcoreUtil.delete(getElement("A"));
		delta.add(new SetHelper.Delta.ElementDeletion("A"));
	}
	
	public void createB() {
		builder.addElement().setElementValue("B");
		delta.add(new SetHelper.Delta.ElementCreation("B"));
	}
	
	public void deleteB() {
		EcoreUtil.delete(getElement("B"));
		delta.add(new SetHelper.Delta.ElementDeletion("B"));
	}
	
	public void createC() {
		builder.addElement().setElementValue("C");
		delta.add(new SetHelper.Delta.ElementCreation("C"));
	}
	
	public void deleteC() {
		EcoreUtil.delete(getElement("C"));
		delta.add(new SetHelper.Delta.ElementDeletion("C"));
	}
	
	public void createD() {
		builder.addElement().setElementValue("D");
		delta.add(new SetHelper.Delta.ElementCreation("D"));
	}
	
	public void deleteD() {
		EcoreUtil.delete(getElement("D"));
		delta.add(new SetHelper.Delta.ElementDeletion("D"));
	}
	
	public void changeABCtoZXY() {
		getElement("A").setValue("Z");
		getElement("B").setValue("X");
		getElement("C").setValue("Y");
		delta.add(new SetHelper.Delta.ElementChange("A", "Z"));
		delta.add(new SetHelper.Delta.ElementChange("B", "X"));
		delta.add(new SetHelper.Delta.ElementChange("C", "Y"));
	}
	
	public void changeZXYtoABC() {
		getElement("Z").setValue("A");
		getElement("X").setValue("B");
		getElement("Y").setValue("C");
		delta.add(new SetHelper.Delta.ElementChange("Z", "A"));
		delta.add(new SetHelper.Delta.ElementChange("X", "B"));
		delta.add(new SetHelper.Delta.ElementChange("Y", "C"));
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
}
