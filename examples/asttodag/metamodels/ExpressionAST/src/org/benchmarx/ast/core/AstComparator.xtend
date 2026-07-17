package org.benchmarx.ast.core

import ast.Expression
import ast.Model
import ast.Number
import ast.Operator
import ast.Variable
import java.util.function.BiConsumer

import static org.junit.jupiter.api.Assertions.*

class AstComparator implements BiConsumer<Model, Model> {
	def static modelToString(Model model) {
		return "AstModel " + expressionToString(model.expr)
	}
	
	override accept(Model expected, Model actual) {
		assertTrue(modelToString(expected).startsWith("AstModel"))
		assertEquals(modelToString(expected), modelToString(actual))
	}
	
	def private static String expressionToString(Expression expression) {
		if(expression === null) {
			return "{ }"
		}
		if (expression instanceof Variable) {
			return "{" + expression.name + ", " + expression.incrementalID + "}"
		}
		
		if (expression instanceof Number) {
			return "{" + expression.value + ", " + expression.incrementalID + "}"
		}
		
		val operator = expression as Operator
		return "{" + operator.op + ", " + expression.incrementalID + ", "
				+ expressionToString(operator.left) + ", " + expressionToString(operator.right) + "}";
	}
}