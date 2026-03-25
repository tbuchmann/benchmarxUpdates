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
		return this;
	}
	
	public CPMBuilder events(int num) {		
		for (int i = 1; i <= num; i++) {
			Event e = f.createEvent();
			e.setNumber(number);
			number++;
			net.get().getElements().add(e);
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
		List<Event> result = net.get().getElements().stream()
				.filter(Event.class::isInstance)
				.map(Event.class::cast)
				.filter(e -> e.getNumber() == number)
				.collect(Collectors.toList());
			
			if (result.size() > 0) return result.get(0);
			else return null;
	}
	
	public static void reset() {
		number = 1;
	}
}
