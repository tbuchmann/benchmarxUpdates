package org.benchmarx.petrinetweighted.core;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.util.EcoreUtil;

import pnw.Edge;
import pnw.Net;
import pnw.PTEdge;
import pnw.Place;
import pnw.PnwFactory;
import pnw.TPEdge;
import pnw.Transition;

/**
 * Builder class for easily creating weighted PetriNets using a Java API, general assumptions for the model:
 * <ul><li>unique names for Places and Transitions</li>
 * <li>not more then one edge between a place/transition or a transition/place pair</li></ul>
 * @author tbuchmann
 *
 */
public class PNWBuilder {
	
	private final Supplier<Net> net;
	private final PnwFactory f = PnwFactory.eINSTANCE;
	private Transition lastTransition;
	// Name -> element caches, avoiding an O(n) scan of the whole model on every
	// find*ByName() call. Kept in sync by every method that creates, renames, or
	// deletes a Place/Transition. Safe: this builder is always constructed
	// against a fresh, empty net (see PNWHelper), and is the only place
	// Places/Transitions are added to it.
	private final java.util.Map<String, Place> placesByName = new java.util.HashMap<>();
	private final java.util.Map<String, Transition> transitionsByName = new java.util.HashMap<>();
	// Total NetElements (places + transitions + PTEdges + TPEdges - everything
	// contained in net.elements, per the eOpposite containment in the ecore
	// model) created/deleted through this builder. Compared against the live
	// net.getElements().size() to detect whether anything bypassed the builder.
	private int knownElementCount = 0;

	public PNWBuilder(Supplier<Net> n) {
		net = n;
	}

	public PNWBuilder netName(String name) {
		net.get().setName(name);
		return this;
	}

	public PNWBuilder place(String name, int numberOfTokens) {
		Place p = f.createPlace();
		net.get().getElements().add(p);
		p.setName(name);
		p.setNoOfTokens(numberOfTokens);
		placesByName.put(name, p);
		knownElementCount++;
		return this;
	}

	public PNWBuilder deletePlace(String place) {
		ArrayList<Edge> edges = new ArrayList<>();
		edges.addAll(findPlaceByName(place).getOutPTEdges());
		edges.addAll(findPlaceByName(place).getInTPEdges());
		for (Edge edge : edges) {
			EcoreUtil.delete(edge);
			knownElementCount--;
		}
		EcoreUtil.delete(findPlaceByName(place));
		placesByName.remove(place);
		knownElementCount--;
		return this;
	}

	public PNWBuilder renamePlace(String oldName, String newName) {
		Place p = findPlaceByName(oldName);
		p.setName(newName);
		placesByName.remove(oldName);
		placesByName.put(newName, p);
		return this;
	}
	
	public PNWBuilder changeTokens(String place, int tokens) {
		findPlaceByName(place).setNoOfTokens(tokens);
		return this;
	}
	
	public PNWBuilder transition(String name, String source, String target, int sourceWeight, int targetWeight) {
		Transition trans = findTransitionByName(name);
		if (trans == null) {
			trans = f.createTransition();
			trans.setName(name);
			net.get().getElements().add(trans);
			transitionsByName.put(name, trans);
			knownElementCount++;
		}
		lastTransition = trans;
		if (source != null) {
			addSource(source, sourceWeight);
		}
		if (target != null) {
			addTarget(target, targetWeight);
		}
		return this;
	}
	
	public PNWBuilder deleteTransition(String transition) {
		ArrayList<Edge> edges = new ArrayList<>();
		edges.addAll(findTransitionByName(transition).getInPTEdges());
		edges.addAll(findTransitionByName(transition).getOutTPEdges());
		for (Edge edge : edges) {
			EcoreUtil.delete(edge);
			knownElementCount--;
		}
		EcoreUtil.delete(findTransitionByName(transition));
		transitionsByName.remove(transition);
		knownElementCount--;
		return this;
	}

	public PNWBuilder renameTransition(String oldName, String newName) {
		Transition t = findTransitionByName(oldName);
		t.setName(newName);
		transitionsByName.remove(oldName);
		transitionsByName.put(newName, t);
		return this;
	}
	
	public PNWBuilder addSource(String place, int weight) {
		return changeSource(place, weight, true);
	}
	public PNWBuilder addTarget(String place, int weight) {
		return changeTarget(place, weight, true);
	}
	public PNWBuilder removeSource(String place) {
		return changeSource(place, -1, false);
	}
	public PNWBuilder removeTarget(String place) {
		return changeTarget(place, -1, false);
	}
	
	public PNWBuilder reconnectPTEdge(String oldPlace, String oldTransition, String newPlace, String newTransition) {
		if (findPlaceByName(newPlace) == null) {
			throw new IllegalArgumentException("No place with name " + newPlace + ".");
		}
		if (findTransitionByName(newTransition) == null) {
			throw new IllegalArgumentException("No transition with name " + newTransition + ".");
		}
		
		PTEdge edge = findPTEdge(oldPlace, oldTransition);
		edge.setFromPlace(findPlaceByName(newPlace));
		edge.setToTransition(findTransitionByName(newTransition));
		return this;
	}
	public PNWBuilder reconnectTPEdge(String oldTransition, String oldPlace, String newTransition, String newPlace) {
		if (findPlaceByName(newPlace) == null) {
			throw new IllegalArgumentException("No place with name " + newPlace + ".");
		}
		if (findTransitionByName(newTransition) == null) {
			throw new IllegalArgumentException("No transition with name " + newTransition + ".");
		}
		
		TPEdge edge = findTPEdge(oldTransition, oldPlace);
		edge.setFromTransition(findTransitionByName(newTransition));
		edge.setToPlace(findPlaceByName(newPlace));
		return this;
	}
	
	public PNWBuilder weightPTEdge(String place, String transition, int weight) {
		findPTEdge(place, transition).setWeight(weight);
		return this;
	}
	public PNWBuilder weightTPEdge(String transition, String place, int weight) {
		findTPEdge(transition, place).setWeight(weight);
		return this;
	}
	
	// Self-healing cache lookup: the cache is authoritative when it hits and still
	// matches the live name, but falls back to a full scan (and re-populates the
	// cache) for elements that entered/were renamed in the net without going
	// through this builder - e.g. places/transitions created by the BX tool
	// itself during propagation, then looked up here in a later idle edit.
	// On a miss, an O(1) size check tells us whether the two caches already
	// mirror every element in the net (i.e. nothing bypassed this builder); if
	// so a miss really does mean "not present" and the O(n) scan is skipped -
	// this is what keeps bulk get-or-create loops (e.g. transition()) from
	// degrading to O(n^2).
	private boolean cachesMirrorModel() {
		return knownElementCount == net.get().getElements().size();
	}

	private Place findPlaceByName(String name) {
		Place cached = placesByName.get(name);
		if (cached != null && name.equals(cached.getName())) return cached;
		if (cached == null && cachesMirrorModel()) return null;
		Optional<Place> found = net.get().getElements().stream()
				.filter(Place.class::isInstance)
				.map(Place.class::cast)
				.filter(a -> name.equals(a.getName()))
				.findFirst();
		found.ifPresent(p -> placesByName.put(name, p));
		return found.orElse(null);
	}

	private Transition findTransitionByName(String name) {
		Transition cached = transitionsByName.get(name);
		if (cached != null && name.equals(cached.getName())) return cached;
		if (cached == null && cachesMirrorModel()) return null;
		Optional<Transition> found = net.get().getElements().stream()
				.filter(Transition.class::isInstance)
				.map(Transition.class::cast)
				.filter(t -> name.equals(t.getName()))
				.findFirst();
		found.ifPresent(t -> transitionsByName.put(name, t));
		return found.orElse(null);
	}
	
	private PTEdge findPTEdge(String place, String transition) {
		Place source = findPlaceByName(place);
		if (source == null) {
			throw new IllegalArgumentException("No place with name " + place + ".");
		}
		
		return source.getOutPTEdges().stream()
				.filter(e -> e.getToTransition().getName().equals(transition))
				.findAny().get();
	}
	private TPEdge findTPEdge(String transition, String place) {
		Transition source = findTransitionByName(transition);
		if (source == null) {
			throw new IllegalArgumentException("No transition with name " + transition + ".");
		}
		
		return source.getOutTPEdges().stream()
				.filter(e -> e.getToPlace().getName().equals(place))
				.findAny().get();
	}
	
	private PNWBuilder changeSource(String place, int weight, boolean add) {
		if (lastTransition == null) {
			throw new IllegalStateException("No transition created to change Source.");
		}
		
		Place s = findPlaceByName(place);
		if (s == null) {
			throw new IllegalArgumentException("No place with name " + place + " in the net.");
		}
		
		PTEdge edge = lastTransition.getInPTEdges().stream()
				.filter(e -> e.getFromPlace().equals(s))
				.findAny().orElse(null);
		if (add && edge == null) {
			PTEdge pte = f.createPTEdge();
			pte.setNet(net.get());
			pte.setWeight(weight);
			pte.setFromPlace(s);
			pte.setToTransition(lastTransition);
			knownElementCount++;
		} else if (!add && edge != null) {
			EcoreUtil.delete(edge);
			knownElementCount--;
		}
		return this;
	}
	private PNWBuilder changeTarget(String place, int weight, boolean add) {
		if (lastTransition == null) {
			throw new IllegalStateException("No transition created to change Source.");
		}
		
		Place t = findPlaceByName(place);
		if (t == null) {
			throw new IllegalArgumentException("No place with name " + place + " in the net.");
		}
		
		TPEdge edge = lastTransition.getOutTPEdges().stream()
				.filter(e -> e.getToPlace().equals(t))
				.findAny().orElse(null);
		if (add && edge == null) {
			TPEdge tpe = f.createTPEdge();
			tpe.setNet(net.get());
			tpe.setWeight(weight);
			tpe.setToPlace(t);
			tpe.setFromTransition(lastTransition);
			knownElementCount++;
		} else if (!add && edge != null) {
			EcoreUtil.delete(edge);
			knownElementCount--;
		}
		return this;
	}
}
