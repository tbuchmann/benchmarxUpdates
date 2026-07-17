package org.benchmarx.gantt.core

import gantt.Activity
import gantt.Dependency
import gantt.Element
import gantt.GanttDiagram
import java.util.ArrayList
import java.util.function.BiConsumer

import static org.junit.jupiter.api.Assertions.*

class GanttComparator implements BiConsumer<GanttDiagram, GanttDiagram> {
	def static modelToString(GanttDiagram diagram) {
		val ArrayList<Element> sortedElements = new ArrayList<Element>(diagram.elements)
		GanttNormalizer.normalize(sortedElements)
		val name = if (diagram.name === null) "" else diagram.name
		return 
		'''
		GanttDiagram «name» «diagram.incrementalID»	{ 
		«sortedElements.map[e | e.elementToString]»
		}
		'''
	}
	
	override accept(GanttDiagram expected, GanttDiagram actual) {
		assertTrue(modelToString(expected).startsWith("GanttDiagram "))
		assertEquals(modelToString(expected), modelToString(actual))
	}
	
	def dispatch private static String elementToString(Activity activity) {
		return 
		'''
		{ 
			Activity-Name: 		«activity.name»
			Activity-Duration: 	«activity.duration»
		}
		'''
	}
	
	def dispatch private static String elementToString(Dependency dependency) {		
		return 
		'''
		{ 
			Dependency-Predecessor: «dependency.predecessor.name»
			Dependency-Successor: 	«dependency.successor.name»
			Dependency-Type: 		«dependency.dependencyType»
			Dependency-Offset: 		«dependency.offset»
		}
		'''
	}
}
