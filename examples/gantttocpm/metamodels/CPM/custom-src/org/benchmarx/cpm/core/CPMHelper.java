package org.benchmarx.cpm.core;

import java.util.Collections;
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

import cpm.Activity;
import cpm.CPMNetwork;
import cpm.Event;

public class CPMHelper {
	
	private Supplier<CPMNetwork> net;
	private CPMBuilder builder;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	
	public CPMHelper(Supplier<CPMNetwork> net, Consumer<EObject> createNode,
			BiConsumer<EReference, List<EObject>> createEdge,
			BiConsumer<EAttribute, List<?>> changeAttribute, Consumer<EObject> deleteNode,
			BiConsumer<EObject, List<EObject>> moveTargetNode, BiConsumer<EReference, List<EObject>> deleteEdge) {
		builder = new CPMBuilder(net, createNode, createEdge);
		this.net = net;
		this.changeAttribute = changeAttribute;
		this.deleteEdge = deleteEdge;
		this.deleteNode = deleteNode;
	}

	public void createEmptyGantt2CPMProcedure() {
		
		builder.name("Gantt2CPM");
	}
	
	public void createEmptyItalyTankRush() {
		
		builder.name("ItalyTankRush");
	}
	
	public void changeIncrementalID() {
		if ("changed".equals(net.get().getIncrementalID())) {
			net.get().setIncrementalID("changed again");
		} else {
			net.get().setIncrementalID("changed");
		}
	}
	
	public void changeIncrementalIDNTimes(int n) {
		for (int i = 0; i < n; i++) {
			changeIncrementalID();
		}
	}

	public void createSimpleNetwork() {
		
		builder.events(6)
			.activity(1, 2, "A1", 3)
			.activity(2, 4, "A1->A2", 0)
			.activity(3, 4, "A2", 4)
			.activity(3, 5, "A2->A3", 2)
			.activity(5, 6, "A3", 5);
	}
	
	public void createCPM2GanttTestCases() {
		
		builder.name("Gantt2CPM")
		.events(2)
		.activity(1, 2, "Gantt2CPMTestCases", 5);
	}
	
	public void addCPM2GanttHelpers() {
		//Precondition: createCPM2GanttTestCases
		
		builder.events(4)
		.activity(3,4,"GanttHelper", 2)
		.activity(5,6,"CPMHelper", 2)
		.activity(1,6,"Gantt2CPMTestCases->CPMHelper", 4)
		.activity(1,3,"Gantt2CPMTestCases->GanttHelper", 0);
	}
	
	public void addCPM2GanttComparators() {
		//Precondition: addCPM2GanttHelpers
		
		builder.events(4)
		.activity(7,8,"GanttComparator", 3)
		.activity(9,10,"CPMComparator", 1)
		.activity(2,8,"Gantt2CPMTestCases->GanttComparator", 0)
		.activity(2,10,"Gantt2CPMTestCases->CPMComparator",0);
	}
	
	public void addCPM2GanttModels() {
		//Precondition: addCPM2GanttComparators
		
		builder.events(4)
		.activity(11,12,"GanttModel", 1)
		.activity(13,14,"CPMModel", 1)
		.activity(12,1,"GanttModel->Gantt2CPMTestCases", 1)
		.activity(14,1,"CPMModel->Gantt2CPMTestCases",2);
	}
	
	public void addCPM2GanttModelsToComparatorDependencies() {
		//Precondition: addCPM2GanttModels
		
		builder.activity(12,7,"GanttModel->GanttComparator", 3);
		builder.activity(14,9,"CPMModel->CPMComparator", 6);
	}
	
	public void deleteCPM2GanttModelsToComparatorDependencies() {
		EcoreUtil.delete(findActivityByName("GanttModel->GanttComparator"));
		EcoreUtil.delete(findActivityByName("CPMModel->CPMComparator"));
	}
	
	public void deleteCPM2GanttHelpers() {
		EcoreUtil.delete(findActivityByName("GanttHelper"));
		EcoreUtil.delete(findActivityByName("CPMHelper"));
		EcoreUtil.delete(findActivityByName("Gantt2CPMTestCases->CPMHelper"));
		EcoreUtil.delete(findActivityByName("Gantt2CPMTestCases->GanttHelper"));
		EcoreUtil.delete(findEventByNumber(3));
		EcoreUtil.delete(findEventByNumber(4));
		EcoreUtil.delete(findEventByNumber(5));
		EcoreUtil.delete(findEventByNumber(6));
	}
	
	// Used for Conflicts tests: renames "GanttHelper" to a value that diverges from what
	// changeGantt2CPMHelperToBuilder sets on the source side, so the two sides genuinely
	// disagree when edited concurrently on the same corresponding element.
	public void renameHelperToAlternative() {
		findActivityByName("GanttHelper").setName("Alternativname");
	}

	public void changeCPM2GanttHelperToBuilder() {
		findActivityByName("GanttHelper").setName("CPMBuilder");
		findActivityByName("CPMHelper").setName("GanttBuilder");
		findActivityByName("Gantt2CPMTestCases->GanttHelper").setName("Gantt2CPMTestCases->CPMBuilder");
		findActivityByName("Gantt2CPMTestCases->CPMHelper").setName("Gantt2CPMTestCases->GanttBuilder");
		
	}
	
	public void changeCPM2GanttModelDuration() {
		findActivityByName("GanttModel").setDuration(0);
		findActivityByName("CPMModel").setDuration(4);
	}
	
	public void changeCPM2GanttTestCasesNameDuration() {
		Activity a = findActivityByName("Gantt2CPMTestCases");
		a.setName("Tests");
		a.setDuration(4);
		Set<Activity> in = new HashSet<Activity>();
		in.addAll(a.getSourceEvent().getIncomingActivities());
		in.addAll(a.getTargetEvent().getIncomingActivities());
		in.remove(a);
		
		Set<Activity> out = new HashSet<Activity>();
		out.addAll(a.getSourceEvent().getOutgoingActivities());
		out.addAll(a.getTargetEvent().getOutgoingActivities());
		out.remove(a);
		for(Activity i : in) {
			i.setName(i.getName().substring(0,i.getName().indexOf("->")+2)+"Tests");
		}
		for(Activity o : out) {
			o.setName("Tests" + o.getName().substring(o.getName().indexOf("->")));
		}
		findActivityByName("Tests->CPMComparator").setDuration(1);
		findActivityByName("Tests->GanttComparator").setDuration(1);
	}
	
	public void changeCPM2GanttModelToComparatorDependencyTypeDurationTargetAndSource() {
		Activity a = findActivityByName("GanttModel->GanttComparator");
		a.setName("CPMModel->CPMBuilder");
		a.setSourceEvent(findEventByNumber(13));
		a.setTargetEvent(findEventByNumber(3));
		a.setDuration(8);
	}
	
	public void changeEventNumbers() {
		List<Integer> ints = net.get().getElements().stream().filter(Event.class::isInstance).map(Event.class::cast).map(e->-e.getNumber()).collect(Collectors.toList());
		Collections.shuffle(ints);
		
		net.get().getElements().stream().filter(Event.class::isInstance).map(Event.class::cast).forEach(e-> {
			e.setNumber(ints.remove(0));
		});
		return;
	}
	
	public void createSimpleTankRush() {
		
		builder.name("ItalyTankRush")
		
			.events(4)
			.activity(1, 2, "spam tanks", 8)
			.activity(3, 4, "win game", 1)
			
			.activity(2, 3, "spam tanks->win game", 180);
	}
	
	public void createComplexTankRush() {
		
		builder.name("ItalyTankRush")
		
			.events(8)
			.activity(1, 2, "build tankbase", 5)
			.activity(3, 4, "research m15", 75)
			.activity(5, 6, "spam tanks", 8)
			.activity(7, 8, "win game", 1)
			
			.activity(1, 3, "build tankbase->research m15", 6)
			.activity(3, 6, "research m15->spam tanks", 84)
			.activity(6, 7, "spam tanks->win game", 180)
			.activity(6, 8, "spam tanks->win game", 181);
	}
	
	// Note: CPMBuilder assigns event numbers from a JVM-wide static counter, so we
	// can't just predict "last known max + 1" from scratch - the counter's absolute
	// value may already be far ahead of this net's own contents. Instead, scan once
	// after the first events() call to learn the actual starting point the counter
	// handed out, then track incrementally: nothing else touches this net's events
	// concurrently within this loop, so each subsequent events(2) call is guaranteed
	// to add exactly the next two sequential numbers. Rescanning and re-sorting all
	// events on every iteration (as an earlier version of this method did) is O(n)
	// per call, i.e. O(n^2 log n) overall - prohibitively slow for large n.
	public void createNActivities(int n, String prefix) {
		Integer max = null;
		for (int i = 1; i <= n; i++) {
			builder.events(2);
			if (max == null) {
				List<Integer> numbers = net.get().getElements().stream()
						.filter(Event.class::isInstance)
						.map(Event.class::cast)
						.map(Event::getNumber)
						.sorted()
						.collect(Collectors.toList());
				max = numbers.get(numbers.size() - 1);
			} else {
				max += 2;
			}
			builder.activity(max - 1, max, prefix + i, 1);
		}
	}

	public void idleDelta() {

	}
	
	private Event findEventByNumber(int number) {
		List<Event> result = net.get().getElements().stream()
				.filter(Event.class::isInstance)
				.map(Event.class::cast)
				.filter(e -> e.getNumber() == number)
				.collect(Collectors.toList());
			
			if (result.size() > 0) return result.get(0);
			else return null;
	}
	
	private Activity findActivityByName(String name) {
		List<Activity> result = net.get().getElements().stream()
				.filter(Activity.class::isInstance)
				.map(Activity.class::cast)
				.filter(e -> e.getName().equals(name))
				.collect(Collectors.toList());
			
			if (result.size() > 0) return result.get(0);
			else return null;
	}
}
