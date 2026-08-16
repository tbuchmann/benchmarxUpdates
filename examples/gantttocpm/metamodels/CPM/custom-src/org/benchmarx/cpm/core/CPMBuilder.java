package org.benchmarx.cpm.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import cpm.Activity;
import cpm.CPMNetwork;
import cpm.CpmFactory;
import cpm.Event;

/**
 * Builder class for easily creating CPM Networks using a Java API
 * general assumption: unique names for Activities
 * @author tbuchmann
 *
 */
public class CPMBuilder {
	
	private final Supplier<CPMNetwork> net;
	private final CpmFactory f = CpmFactory.eINSTANCE;
	private static int number = 1;
	private Consumer<EObject> createNode;
	private BiConsumer<EReference, List<EObject>> createEdge;
	// Cache of event number -> Event, avoiding an O(n) scan of the whole model on
	// every findEventByNumber() call (previously O(n) per activity() call, i.e.
	// O(n^2) total when building n activities - prohibitively slow at scale).
	// Safe: this builder is always constructed against a fresh, empty net, and is
	// the only place events are added to it.
	private final java.util.Map<Integer, Event> eventsByNumber = new java.util.HashMap<>();
	
//	public CPMBuilder(String name) {
//		net = f.createCPMNetwork();
//		net.setName(name);
//	}
	
	public CPMBuilder(Supplier<CPMNetwork> network, Consumer<EObject> cn, BiConsumer<EReference, List<EObject>> ce) {
		net = network;
		createNode = cn;
		createEdge = ce;
	}
	
	public CPMBuilder name(String name) {
		net.get().setName(name);
		return this;
	}
	
	public CPMBuilder event() {
		Event e = f.createEvent();
		e.setNumber(number);
		number++;
		net.get().getElements().add(e);
		eventsByNumber.put(e.getNumber(), e);
		return this;
	}

	public CPMBuilder events(int num) {
		for (int i = 1; i <= num; i++) {
			Event e = f.createEvent();
			e.setNumber(number);
			number++;
			net.get().getElements().add(e);
			eventsByNumber.put(e.getNumber(), e);
		}
		return this;
	}
	
	public CPMBuilder activity(int e1, int e2, String name, int duration) {
		Event s = findEventByNumber(e1);
		Event t = findEventByNumber(e2);
		Activity a = f.createActivity();
		net.get().getElements().add(a);		
		a.setName(name);
		a.setDuration(duration);
		a.setSourceEvent(s);
		a.setTargetEvent(t);
		return this;
	}
	
	public CPMNetwork end() {
		return net.get();
	}
	
	private Event findEventByNumber(int number) {
		return eventsByNumber.get(number);
	}
	
	public static void reset() {
		number = 1;
	}
}
