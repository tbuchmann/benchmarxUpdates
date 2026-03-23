package org.benchmarx.examples.familiestopersons.testsuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.benchmarx.BXTool;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import Families.FamilyRegister;
import Persons.PersonRegister;

public class BXToolParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (!(parameterContext.getDeclaringExecutable() instanceof Constructor)) {
			return false;
		}
		Parameter parameter = parameterContext.getParameter();
		return BXTool.class.isAssignableFrom(parameter.getType());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		List<BXTool<FamilyRegister, PersonRegister, Decisions>> toolList =
				new ArrayList<>(FamiliesToPersonsTestCase.tools());
		int index = getInvocationIndex(extensionContext);
		return toolList.stream()
				.skip(index)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No tool available at index " + index));
	}

	/**
	 * For @ParameterizedTest, JUnit 5 sets the display name to "[N] ..." where N is
	 * the 1-based invocation index. Parse N and return as a 0-based list index.
	 * Falls back to 0 for plain @Test methods.
	 */
	private int getInvocationIndex(ExtensionContext extensionContext) {
		String displayName = extensionContext.getDisplayName();
		if (displayName.startsWith("[")) {
			int end = displayName.indexOf(']');
			if (end > 0) {
				try {
					return Integer.parseInt(displayName.substring(1, end)) - 1;
				} catch (NumberFormatException ignored) {}
			}
		}
		return 0;
	}
}
