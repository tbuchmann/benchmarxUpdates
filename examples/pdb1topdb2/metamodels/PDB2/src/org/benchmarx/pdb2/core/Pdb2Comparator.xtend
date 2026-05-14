package org.benchmarx.pdb2.core

import java.util.ArrayList
import java.util.List
import java.util.function.BiConsumer
import pdb2.Database
import pdb2.Person

class Pdb2Comparator implements BiConsumer<Database, Database> {
	
	Pdb2Normaliser comparator
	
	new (){
		comparator = new Pdb2Normaliser();
	}
	
//	override assertEquals(Database expected, Database actual) {
//		assertTrue(personsToString(expected).startsWith("Pdb2Database"))
//		assertEquals(personsToString(expected), personsToString(actual))
//	}
	
	def personsToString(Database persons) {
		return '''
		Pdb2Database «persons.name» {
			persons = [
				«val List<Person> sortedList = new ArrayList<Person>(persons.persons)»
				«comparator.normalize(sortedList)»
				«FOR p: sortedList SEPARATOR ","»
				«p.id» {
					  name = "«p.name»"
					, birthday = "«p.birthday»"
					, placeOfBirth = "«p.placeOfBirth»"
					, incrementalID = "«p.incrementalID»"
				}
				«ENDFOR»
			]
		}
		'''
	}
	
	override accept(Database expected, Database actual) {
		val expectedStr = personsToString(expected)
		val actualStr = personsToString(actual)
		if (!expectedStr.startsWith("Pdb2Database"))
			throw new IllegalStateException("Expected string to start with 'Pdb2Database' but was: " + expectedStr)
		if (expectedStr != actualStr)
			throw new AssertionError("Expected:\n" + expectedStr + "\nbut was:\n" + actualStr)
	}
	
}