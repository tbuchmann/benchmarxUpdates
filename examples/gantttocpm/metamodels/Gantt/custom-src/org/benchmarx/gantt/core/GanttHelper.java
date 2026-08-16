package org.benchmarx.gantt.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import gantt.Activity;
import gantt.Dependency;
import gantt.DependencyType;
import gantt.Element;
import gantt.GanttDiagram;

public class GanttHelper {
	private GanttBuilder builder;
	private Supplier<GanttDiagram> diag;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
		
	public GanttHelper(Supplier<GanttDiagram> rootSupplier, Consumer<EObject> createSourceNode,
			BiConsumer<EReference, List<EObject>> createSourceEdge,
			BiConsumer<EAttribute, List<?>> changeSourceAttribute, Consumer<EObject> deleteSourceNode,
			BiConsumer<EObject, List<EObject>> moveSourceNode, BiConsumer<EReference, List<EObject>> deleteSourceEdge) {
		builder = new GanttBuilder(rootSupplier);
		this.diag = rootSupplier;
		this.changeAttribute = changeSourceAttribute;
		this.deleteEdge = deleteSourceEdge;
		this.deleteNode = deleteSourceNode;
	}

	public void createEmptyGantt2CPMProcedure() {
		
		builder.name("Gantt2CPM");
	}
	
	public void createEmptyItalyTankRush() {
		
		builder.name("ItalyTankRush");
	}
	
	public void createGantt2CPMTestCases() {
		
		builder.name("Gantt2CPM")
		.activity("Gantt2CPMTestCases", 5);
	}
	
	public void changeIncrementalID() {
		if ("changed".equals(diag.get().getIncrementalID())) {
			diag.get().setIncrementalID("changed again");
		} else {
			diag.get().setIncrementalID("changed");
		}
	}
	
	public void changeIncrementalIDNTimes(int n) {
		for (int i = 0; i < n; i++) {
			changeIncrementalID();
		}
	}

	public void addGantt2CPMHelpers() {
		//Precondition: createGantt2CPMTestCases
		
		builder.activity("GanttHelper", 2)
		.activity("CPMHelper", 2)
		.startend("Gantt2CPMTestCases", "CPMHelper", 4)
		.startstart("Gantt2CPMTestCases", "GanttHelper", 0);
	}
	
	public void addGantt2CPMComparators() {
		//Precondition: createGantt2CPMTestCases
		
		builder.activity("GanttComparator", 3)
		.activity("CPMComparator", 1)
		.endend("Gantt2CPMTestCases", "GanttComparator", 0)
		.endend("Gantt2CPMTestCases","CPMComparator",0);
	}
	
	public void addGantt2CPMModels() {
		//Precondition: createGantt2CPMTestCases
		
		builder.activity("GanttModel", 1)
		.activity("CPMModel", 1)
		.endstart("GanttModel", "Gantt2CPMTestCases", 1)
		.endstart("CPMModel","Gantt2CPMTestCases",2);
	}
	
	public void addGantt2CPMModelsToComparatorDependencies() {
		//Precondition: addGantt2CPMModels, addGantt2CPMComparators
		
		builder.endstart("GanttModel", "GanttComparator", 3);
		builder.endstart("CPMModel", "CPMComparator", 6);
	}
	
	public void deleteGantt2CPMModelsToComparatorDependencies() {
		Activity ganttModel = findActivityByName("GanttModel");
		Activity cpmModel = findActivityByName("CPMModel");
		Set<Dependency> s = new HashSet<Dependency>();
		s.addAll(ganttModel.getIncomingDependencies());
		s.addAll(ganttModel.getOutgoingDependencies());
		s.addAll(cpmModel.getIncomingDependencies());
		s.addAll(cpmModel.getOutgoingDependencies());
		for(Dependency e : s) {
			if(e.getSuccessor().getName().equals("CPMComparator") || e.getSuccessor().getName().equals("GanttComparator"))
				EcoreUtil.delete(e,true);
		}
	}
	
	public void deleteGantt2CPMHelpers() {
		Activity a = findActivityByName("GanttHelper");
		Activity b = findActivityByName("CPMHelper");
		Set<Element> s = new HashSet<Element>();
		s.addAll(a.getIncomingDependencies());
		s.addAll(a.getOutgoingDependencies());
		s.addAll(b.getIncomingDependencies());
		s.addAll(b.getOutgoingDependencies());
		s.add(a); s.add(b);
		for(Element e : s) {
			EcoreUtil.delete(e,true);
		}
	}
	
	public void changeGantt2CPMHelperToBuilder() {
		findActivityByName("GanttHelper").setName("CPMBuilder");
		findActivityByName("CPMHelper").setName("GanttBuilder");
	}
	
	public void changeGantt2CPMModelDuration() {
		findActivityByName("GanttModel").setDuration(0);
		findActivityByName("CPMModel").setDuration(4);
	}
	
	public void changeGantt2CPMTestCasesNameDuration() {
		findActivityByName("Gantt2CPMTestCases").setName("Tests");
		findActivityByName("Tests").setDuration(4);
		List<Dependency> out = findActivityByName("Tests").getOutgoingDependencies();
		for(Dependency d : out) {
			if(d.getSuccessor().getName().equals("CPMComparator") || d.getSuccessor().getName().equals("GanttComparator")) {
				d.setOffset(1);
			}
		}
	}
	
	public void changeGantt2CPMModelToComparatorDependencyTypeDurationTargetAndSource() {
		List<Dependency> out = new ArrayList<Dependency>();
		out.addAll(findActivityByName("GanttModel").getOutgoingDependencies());
		for(Dependency d : out) {
			if(d.getSuccessor().getName().equals("GanttComparator")) {
				d.setDependencyType(DependencyType.START_START);
				d.setPredecessor(findActivityByName("CPMModel"));
				d.setSuccessor(findActivityByName("CPMBuilder"));
				d.setOffset(8);
			}
		}
	}
	
	public void createSimpleTankRush() {
		
		builder.name("ItalyTankRush")
		
			.activity("spam tanks", 8)
			.activity("win game", 1)
			
			.endstart("spam tanks", "win game", 180);
	}
	
	public void createComplexTankRush() {
		
		builder.name("ItalyTankRush")
		
			.activity("build tankbase", 5)
			.activity("research m15", 75)
			.activity("spam tanks", 8)
			.activity("win game", 1)
			
			.startstart("build tankbase", "research m15", 6)
			.startend("research m15", "spam tanks", 84)
			.endstart("spam tanks", "win game", 180)
			.endend("spam tanks", "win game", 181);
	}
	
	public void createNActivities(int n, String prefix) {
		for (int i = 1; i <= n; i++) {
			builder.activity(prefix + i, 1);
		}
	}

	public void idleDelta() {

	}
	
	private Activity findActivityByName(String name) {
		List<Activity> result = diag.get().getElements().stream()
				.filter(Activity.class::isInstance)
				.map(Activity.class::cast)
				.filter(a -> a.getName().equals(name))
				.collect(Collectors.toList());
			
		if (result.size() > 0) return result.get(0);
		else return null;
	}
}
