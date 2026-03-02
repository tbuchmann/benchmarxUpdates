package org.benchmarx.osets.core;

import java.util.function.Supplier;

import osets.Element;
import osets.MyOrderedSet;
import osets.OsetsFactory;

public class OsetMyOrderedSetBuilder {
	private Supplier<MyOrderedSet> set;
	private Element last; // last element inserted in the oset, the position in the oset is arbitrary
	private OsetsFactory f = OsetsFactory.eINSTANCE;
	
	OsetMyOrderedSetBuilder(Supplier<MyOrderedSet> set) {
		this.set = set;
	}
	
	OsetMyOrderedSetBuilder mySetName(String name) {
		set.get().setName(name);
		return this;
	}
	
	OsetMyOrderedSetBuilder next() {
		Element temp = f.createElement();
		if(!set.get().getElements().isEmpty()) {
			if(last == null)
				last = set.get().getElements().get(set.get().getElements().size()-1);
			temp.setOrderedSet(set.get());
			last.setNext(temp);
		} else {
			temp.setOrderedSet(set.get());
		}
		
		last = temp;
		return this;
	}
	
	OsetMyOrderedSetBuilder setElementValue(String val) {
		last.setValue(val);
		return this;
	}
	
	OsetMyOrderedSetBuilder insertNewElementBefore(String val) {
		Element find = null;
		for(Element e : set.get().getElements()) {
			if(e.getValue().equals(val)) {
				find = e;
				break;
			}
		}
		if(find != null) {
			last = f.createElement();
			last.setOrderedSet(set.get());
			last.setPrevious(find.getPrevious());
			last.setNext(find);
		}
		return this;
	}
	
	OsetMyOrderedSetBuilder insertNewElementAfter(String val) {
		Element find = null;
		for(Element e : set.get().getElements()) {
			if(e.getValue().equals(val)) {
				find = e;
				break;
			}
		}
		if(find != null) {
			last = f.createElement();
			last.setOrderedSet(set.get());
			last.setNext(find.getNext());
			last.setPrevious(find);
		}
		return this;
	}
}
