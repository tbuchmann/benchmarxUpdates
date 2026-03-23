package org.benchmarx.bags1.core

import bags1.MyBag

import static org.junit.Assert.*
import bags1.Element
import java.util.ArrayList
import java.util.function.BiConsumer

class Bag1Comparator implements BiConsumer<MyBag, MyBag> {
	
	ElementNormaliser comparator
	
	new (){
		comparator = new ElementNormaliser();
	}
	override accept(MyBag expected, MyBag actual) {
		assertTrue(bagToString(expected).startsWith("Bag1"))
		assertEquals(bagToString(expected), bagToString(actual))
	}
	
	def bagToString(MyBag b) {
		return '''
		Bag1 {
			elements = [
				«val sortedList = new ArrayList<Element>(b.elements)»
				«comparator.normalize(sortedList)»
				«FOR e: sortedList SEPARATOR ","»
				{
					  value = "«e.value»",
					  incrementalID = "«e.incrementalID»"
				}
				«ENDFOR»
			]
		}
		'''
	}
	
}