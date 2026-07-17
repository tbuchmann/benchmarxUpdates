package org.benchmarx.petrinetweighted.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import pnw.Net;

public class PNWHelper {
	private PNWBuilder builder;
	private Supplier<Net> net;
	private BiConsumer<EAttribute /* attribute type */, List<?> /* [owning node, old value, new value] */> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EObject, List<EObject>> moveNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	private BiConsumer<EReference, List<EObject>> createEdge;
	
	public PNWHelper(Supplier<Net> net, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge, BiConsumer<EAttribute, List<?>> changeAttribute,
			Consumer<EObject> deleteNode, BiConsumer<EObject, List<EObject>> moveNode,
			BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new PNWBuilder(net);//, createNode, createEdge);
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
	// Used for Conflicts tests: renames to a value that diverges from what SRC's
	// renameToFactoryModel() sets, so the two sides genuinely disagree when edited
	// concurrently on the same corresponding net name.
	public void renameToAlternativeModel() {
		builder.netName("AlternativeModel");
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
			
			.transition("1", "B", null, 13, 0)
			.transition("2", null, "C", 0, 21)
			.transition("3", "D",  "E", 34, 55)
			.transition("4", "F", "G", 89, 144);
	}	
	public void createComplexLettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 9)
			.place("B", 16)
			.place("C", 25)
			.place("D", 36)
			
			.transition("1", "B", null, 1, 0)
			.transition("2", null, "C", 0, 1).addTarget("D", 1)
			.transition("3", "A", "D", 2, 4).addSource("B", 3)
			.transition("4", "B", "B", 5, 6).addSource("D", 7).addTarget("D", 8);
	}
	
	public void createPTPLettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 1)
			.place("B", 0)
			
			.transition("1", "A", "B", 1, 1);
	}
	public void extendPTPLettersDigits() {
		builder
			.place("C", 0)
			
			.transition("1", "C", null, 1, 1)
			.transition("2", "A", "B", 1, 1);
	}
	public void furtherExtendPTPLettersDigits() {
		builder
			.transition("1", "B", null, 1, 1)
			.transition("2", null, "C", 1, 1);
	}
	public void reducePTPExtendedLettersDigits() {
		builder
			.deletePlace("C")

			.deleteTransition("2");
	}
	public void reducePTPFurtherExtendedLettersDigits() {
		builder
			.transition("1", null, null, -1, -1).removeSource("B")
			.transition("2", null, null, -1, -1).removeTarget("C");
	}
	
	public void create1234LettersDigits() {
		builder
			.netName("LettersAndDigits")
		
			.place("A", 1)
			.place("B", 2)
			.place("C", 3)
			.place("D", 4)
			
			.transition("1", "A", "B", 1, 1)
			.transition("2", "C", "D", 1, 1).addSource("B", 1);
	}	
	public void construct9012LettersDigits() {
		builder
			.changeTokens("A", 9)
			.changeTokens("B", 0)
			.changeTokens("C", 1)
			.changeTokens("D", 2)
			
			.reconnectPTEdge("B", "2", "D", "1")
			.reconnectTPEdge("2", "D", "2", "A")
		
			.renamePlace("A", "tmp")
			.renamePlace("D", "A")
			.renamePlace("tmp", "D")
			.renamePlace("B", "E")
			
			.renameTransition("1", "tmp")
			.renameTransition("2", "1")
			.renameTransition("tmp", "2");
	}
	
	public void weightA1BWith42() {
		builder
			.weightPTEdge("A", "1", 4)
			.weightTPEdge("1", "B", 2);
	}
	public void weightA1BWith73() {
		builder
			.weightPTEdge("A", "1", 7)
			.weightTPEdge("1", "B", 3);
	}
	public void weightA3DWith24() {
		builder
			.weightPTEdge("A", "3", 2)
			.weightTPEdge("3", "D", 4);
	}
	public void weightB2With9() {
		builder
			.weightPTEdge("B", "2", 9);
	}
	
	public void idleDelta() {	
	}
}
