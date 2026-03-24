package org.benchmarx.petrinet.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import pn.Net;

public class PNHelper {
	
	private PNBuilder builder;
	private Supplier<Net> net;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public PNHelper(Supplier<Net> net, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new PNBuilder(net);//, createNode, createEdge);
		this.net = net;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
		this.moveNode = moveNode;
		this.createEdge = createEdge;
	}
	
	public void renameToLettersAndDigits() {		
		builder.netName("LettersAndDigits");
	}	
	public void renameToFactoryModel() {		
		builder.netName("FactoryModel");
	}
	
	public void changeIncrementalID() {
		if ("changed".equals(net.get().getIncrementalID())) {
			net.get().setIncrementalID("changed again");
		} else {
			net.get().setIncrementalID("changed");
		}
	}
	
	public void createSimpleLettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 0)
			.place("B", 1)
			.place("C", 1)
			.place("D", 2)
			.place("E", 3)
			.place("F", 5)
			.place("G", 8)
			
			.transition("1", "B", null)
			.transition("2", null, "C")
			.transition("3", "D", "E")
			.transition("4", "F", "G");
	}	
	public void createComplexLettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 9)
			.place("B", 16)
			.place("C", 25)
			.place("D", 36)
			
			.transition("1", "B", null)
			.transition("2", null, "C").addTarget("D")
			.transition("3", "A", "D").addSource("B")
			.transition("4", "B", "B").addSource("D").addTarget("D");
	}
	
	public void createPTPLettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 1)
			.place("B", 0)
			
			.transition("1", "A", "B");
	}	
	public void extendPTPLettersDigits() {
		builder
			.place("C", 0)
			
			.transition("1", "C", null)
			.transition("2", "A", "B");
	}	
	public void reducePTPExtendedLettersDigits() {
		builder
			.deletePlace("C")

			.deleteTransition("2");
	}
	
	public void create1234LettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 1)
			.place("B", 2)
			.place("C", 3)
			.place("D", 4)
			
			.transition("1", "A", "B")
			.transition("2", "C", "D").addSource("B");
	}	
	public void construct5678LettersDigits() {		
		builder
			.changeTokens("A", 5)
			.changeTokens("B", 6)
			.changeTokens("C", 7)
			.changeTokens("D", 8)
			
			.transition("1", "C", null)
			.transition("2", "D", null).removeSource("B").removeTarget("D")
		
			.renamePlace("A", "tmp")
			.renamePlace("D", "A")
			.renamePlace("tmp", "D")
			.renamePlace("B", "E")
			
			.renameTransition("1", "tmp")
			.renameTransition("2", "1")
			.renameTransition("tmp", "2");
	}
	
	public void idleDelta() {	
	}
}
