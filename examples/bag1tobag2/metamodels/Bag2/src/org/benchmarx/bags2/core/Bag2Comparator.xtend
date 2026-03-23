package org.benchmarx.bags2.core

import bags2.Element
import bags2.MyBag
import java.util.ArrayList
import java.util.function.BiConsumer

import static org.junit.Assert.*

class Bag2Comparator implements BiConsumer<MyBag, MyBag> {
	
	ElementNormaliser comparator
	
	new (){
		comparator = new ElementNormaliser();
	}
	override accept(MyBag expected, MyBag actual) {
		assertTrue(bagToString(expected).startsWith("Bag2"))
		assertEquals(bagToString(expected), bagToString(actual))
	}
	
	def bagToString(MyBag b) {
		return '''
		Bag2 {
			elements = [
				«val sortedList = new ArrayList<Element>(b.elements)»
				«comparator.normalize(sortedList)»
				«FOR e: sortedList SEPARATOR ","»
				{
					  value = "«e.value»",
					  multiplicity = «e.multiplicity»,
					  incrementalID = "«e.incrementalID»"
				}
				«ENDFOR»
			]
		}
		'''
	}
	
}
