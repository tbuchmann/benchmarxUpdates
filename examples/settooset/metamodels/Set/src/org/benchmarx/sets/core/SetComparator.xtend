package org.benchmarx.sets.core

import java.util.ArrayList
import java.util.List
import java.util.function.BiConsumer
import sets.Element
import sets.MySet

import static org.junit.Assert.*

class SetComparator implements BiConsumer<MySet, MySet> {	
	ElementNormaliser comparator
	
	new (){
		comparator = new ElementNormaliser();
	}
	
	override accept(MySet expected, MySet actual) {
		assertTrue(mySetToString(expected).startsWith("SetsMySet"))
		assertEquals(mySetToString(expected), mySetToString(actual))
	}
	
	def mySetToString(MySet set) {
		val List<Element> elements = new ArrayList<Element>(set.elements)
		comparator.normalize(elements)
		return "SetsMySet " + set.name + " " + set.incrementalID + " elements: " + elements.map[value].join(", ")
	}
}