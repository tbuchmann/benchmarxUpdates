package org.benchmarx.examples.familiestopersons.testsuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.benchmarx.BXTool;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.ParameterizedTest;

import Families.FamilyRegister;
import Persons.PersonRegister;

/**
 * BXToolParameterResolver — kept for @ExtendWith compatibility but intentionally
 * resolves nothing.
 *
 * With no-arg constructors on all test subclasses and
 * "@ParameterizedTest @MethodSource("tools")" on every test method, JUnit 5
 * provides the BXTool via the method source — this resolver must not interfere.
 *
 * Rules:
 *  - Constructor parameters: never supported (no-arg constructors are used).
 *  - Method parameters of @ParameterizedTest: already supplied by @MethodSource.
 *  - All other cases: not supported either (no plain @Test methods need a BXTool).
 */
public class BXToolParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// Reject constructor injection
		if (parameterContext.getDeclaringExecutable() instanceof Constructor) {
			return false;
		}
		// Reject method parameters of @ParameterizedTest methods
		// (the tool is already provided by @MethodSource)
		if (parameterContext.getDeclaringExecutable() instanceof Method) {
			Method method = (Method) parameterContext.getDeclaringExecutable();
			if (method.isAnnotationPresent(ParameterizedTest.class)) {
				return false;
			}
		}
		// Resolve BXTool parameters for plain @Test methods (safety net, not currently used)
		return BXTool.class.isAssignableFrom(parameterContext.getParameter().getType());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return new java.util.ArrayList<>(FamiliesToPersonsTestCase.tools()).get(0);
	}
}