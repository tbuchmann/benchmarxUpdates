package org.benchmarx.petrinet.core;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.util.EcoreUtil;

import pn.Net;
import pn.Place;
import pn.PnFactory;
import pn.Transition;

/**
 * Builder class for easily creating (unweighted) PetriNets using a Java API, general assumptions for the model:
 * <ul><li>unique names for Places and Transitions</li>
 * <li>not more then one edge between a place/transition or a transition/place pair</li></ul>
 * @author tbuchmann
 *
 */
public class PNBuilder {
	private final Supplier<Net> net;
	private final PnFactory f = PnFactory.eINSTANCE;
	private Transition lastTransition;
	// Name -> element caches, avoiding an O(n) scan of the whole model on every
	// find*ByName() call (previously O(n) per place()/transition() call, i.e.
	// O(n^2) total when building n units - prohibitively slow at scale). Kept in
	// sync by every method that creates, renames, or deletes a Place/Transition.
	// Safe: this builder is always constructed against a fresh, empty net (see
	// PNHelper), and is the only place Places/Transitions are added to it.
	private final java.util.Map<String, Place> placesByName = new java.util.HashMap<>();
	private final java.util.Map<String, Transition> transitionsByName = new java.util.HashMap<>();

	public PNBuilder(Supplier<Net> n) {
		net = n;
	}

	public PNBuilder netName(String name) {
		net.get().setName(name);
		return this;
	}

	public PNBuilder place(String name, int numberOfTokens) {
		Place p = f.createPlace();
		p.setName(name);
		p.setNoOfTokens(numberOfTokens);
		net.get().getElements().add(p);
		placesByName.put(name, p);
		return this;
	}

	public PNBuilder deletePlace(String place) {
		EcoreUtil.delete(findPlaceByName(place));
		placesByName.remove(place);
		return this;
	}

	public PNBuilder renamePlace(String oldName, String newName) {
		Place p = findPlaceByName(oldName);
		p.setName(newName);
		placesByName.remove(oldName);
		placesByName.put(newName, p);
		return this;
	}
	
	public PNBuilder changeTokens(String place, int tokens) {
		findPlaceByName(place).setNoOfTokens(tokens);
		return this;
	}
	
	public PNBuilder transition(String name, String source, String target) {
		Transition trans = findTransitionByName(name);
		if (trans == null) {
			trans = f.createTransition();
			net.get().getElements().add(trans);
			trans.setName(name);
			transitionsByName.put(name, trans);
		}
		lastTransition = trans;
		if (source != null) {
			addSource(source);
		}
		if (target != null) {
			addTarget(target);
		}
		return this;
	}
	
	public PNBuilder deleteTransition(String transition) {
		EcoreUtil.delete(findTransitionByName(transition));
		transitionsByName.remove(transition);
		return this;
	}

	public PNBuilder renameTransition(String oldName, String newName) {
		Transition t = findTransitionByName(oldName);
		t.setName(newName);
		transitionsByName.remove(oldName);
		transitionsByName.put(newName, t);
		return this;
	}
	
	public PNBuilder addSource(String place) {
		return changeSource(place, true);
	}
	public PNBuilder addTarget(String place) {
		return changeTarget(place, true);
	}
	public PNBuilder removeSource(String place) {
		return changeSource(place, false);
	}
	public PNBuilder removeTarget(String place) {
		return changeTarget(place, false);
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
		return placesByName.size() + transitionsByName.size() == net.get().getElements().size();
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
	
	private PNBuilder changeSource(String place, boolean add) {
		if (lastTransition == null) {
			throw new IllegalStateException("No transition created to change Source.");
		}
		
		Place s = findPlaceByName(place);
		if (s == null) {
			throw new IllegalArgumentException("No place with name " + place + " in the net.");
		}
		
		if (add && !lastTransition.getSrcP2T().contains(s)) {
			lastTransition.getSrcP2T().add(s);
		} else if (!add && lastTransition.getSrcP2T().contains(s)) {
			lastTransition.getSrcP2T().remove(s);
		}
		return this;
	}
	private PNBuilder changeTarget(String place, boolean add) {
		if (lastTransition == null) {
			throw new IllegalStateException("No transition created to change Source.");
		}
		
		Place t = findPlaceByName(place);
		if (t == null) {
			throw new IllegalArgumentException("No place with name " + place + " in the net.");
		}
		
		if (add && !lastTransition.getTrgT2P().contains(t)) {
			lastTransition.getTrgT2P().add(t);
		} else if (!add && lastTransition.getTrgT2P().contains(t)) {
			lastTransition.getTrgT2P().remove(t);
		}
		return this;
	}
}
