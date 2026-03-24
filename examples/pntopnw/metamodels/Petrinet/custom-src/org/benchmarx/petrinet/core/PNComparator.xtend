package org.benchmarx.petrinet.core

import java.util.ArrayList
import java.util.List
import java.util.function.BiConsumer
import pn.Net
import pn.NetElement
import pn.Place
import pn.Transition

import static org.junit.Assert.*

class PNComparator implements BiConsumer<Net, Net> {
	def static netToString(Net net) {
		val ArrayList<NetElement> sortedElements = new ArrayList<NetElement>(net.elements)
		PNNormalizer.normalize(sortedElements)
		val name = if (net.name === null) "" else net.name
		return "PetriNet " + name + " " + net.incrementalID
				+ " {" + sortedElements.map[e | e.elementToString].join(", ") + "}"
	}
	
	override accept(Net expected, Net actual) {
		assertTrue(netToString(expected).startsWith("PetriNet "))
		assertEquals(netToString(expected), netToString(actual))
	}
	
	def dispatch private static String elementToString(Place place) {
		return "{" + place.name + ", " + place.noOfTokens + "}"
	}
	
	def dispatch private static String elementToString(Transition transition) {	
		return "{" + transition.name + ", "
				+ placeRefsToString(transition.srcP2T) + ", " + placeRefsToString(transition.trgT2P) + "}"
	}
	
	def private static String placeRefsToString(List<Place> places) {
		return "{" + places.map[e | e.name].sort.join(", ") + "}"
	}
}