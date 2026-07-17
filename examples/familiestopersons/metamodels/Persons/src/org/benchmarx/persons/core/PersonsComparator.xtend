package org.benchmarx.persons.core

import Persons.Male
import Persons.Person
import Persons.PersonRegister
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.List
import java.util.function.BiConsumer
import java.util.logging.Logger

class PersonsComparator implements BiConsumer<PersonRegister, PersonRegister> {
	static val LOG = Logger.getLogger(PersonsComparator.name)

	PersonNormaliser comparator
	PersonStructuralNormaliser structuralComparator
	boolean checkAttributeValues
	
	new (){
		comparator = new PersonNormaliser();
		structuralComparator = new PersonStructuralNormaliser();
		checkAttributeValues = true
	}
	
	new (boolean checkAttributeValues){
		comparator = new PersonNormaliser();
		structuralComparator = new PersonStructuralNormaliser();
		this.checkAttributeValues = checkAttributeValues
	}
	
	override void accept(PersonRegister expected, PersonRegister actual) {
		val expectedStr = personsToString(expected)
		val actualStr = personsToString(actual)
		if (!expectedStr.startsWith("PersonRegister")) {
			val msg = "Invalid expected PersonRegister: " + expectedStr
			LOG.severe(msg)
			throw new AssertionError(msg)
		}
		if (expectedStr != actualStr) {
			val msg = "PersonRegister mismatch:\nExpected: " + expectedStr + "\nActual:   " + actualStr
			LOG.severe(msg)
			throw new AssertionError(msg)
		}
	}
	
	def personsToString(PersonRegister persons) {
		return '''
		PersonRegister {
			persons = [
				«val List<Person> sortedList = new ArrayList<Person>(persons.persons)»
				«IF checkAttributeValues»
				«comparator.normalize(sortedList)»
				«ELSE»«structuralComparator.normalize(sortedList)»«ENDIF»
				«FOR p: sortedList SEPARATOR ","»
				«IF p instanceof Male»
				 Male«IF checkAttributeValues» {
				          fullName = "«p.name»"
				          , birthday = "«p.birthday.toMyString»"
				 }«ELSE»«ENDIF»
				«ELSE»
				 Female«IF checkAttributeValues» {   
				          fullName = "«p.name»"
				        , birthday = "«p.birthday.toMyString»"
				 }«ELSE»«ENDIF»
				«ENDIF»
				«ENDFOR»
			]
		}
		'''
	}

	def toMyString(Date d) {
		val sm = new SimpleDateFormat("yyyy-MM-dd")
		return sm.format(d)
	}	
}
