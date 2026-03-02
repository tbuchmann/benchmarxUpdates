package org.benchmarx.sets.core;

import java.util.function.Supplier;

import sets.Element;
import sets.MySet;
import sets.SetsFactory;

public class SetMySetBuilder {
	
	private Supplier<MySet> set;
	private Element last;
	private SetsFactory f = SetsFactory.eINSTANCE;
	
	SetMySetBuilder(Supplier<MySet> set) {
		this.set = set;
	}
	
	SetMySetBuilder mySetName(String name) {
		set.get().setName(name);
		return this;
	}
	
	SetMySetBuilder addElement() {
		last = f.createElement();
		last.setSet(set.get());
		return this;
	}
	
	SetMySetBuilder setElementValue(String val) {
		last.setValue(val);
		return this;
	}
}
