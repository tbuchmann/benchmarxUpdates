package org.benchmarx.dsl.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.benchmarx.dsl.benchmarxDSL.AnyPostcondition;
import org.benchmarx.dsl.benchmarxDSL.BatchFwd;
import org.benchmarx.dsl.benchmarxDSL.BxModel;
import org.benchmarx.dsl.benchmarxDSL.Concurrent;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(BenchmarxDSLInjectorProvider.class)
public class GrammarParsingTest {

	@Inject ParseHelper<BxModel> parseHelper;

	@Test
	void minimal_problem_parses() throws Exception {
		BxModel m = parseHelper.parse("""
				problem MyBX {
				  source metamodel "src/My.ecore"
				  target metamodel "tgt/My.ecore"
				}
				""");
		assertNotNull(m);
		assertEquals(1, m.getProblems().size());
		assertEquals("MyBX", m.getProblems().get(0).getName());
	}

	@Test
	void state_with_nested_children_parses() throws Exception {
		BxModel m = parseHelper.parse("""
				problem P {
				  source metamodel "src.ecore"
				  target metamodel "tgt.ecore"
				}
				state myState : P.source {
				  FamilyRegister {
				    families {
				      Family(name = "Simpson") {}
				    }
				  }
				}
				""");
		assertNotNull(m);
		assertEquals(1, m.getStates().size());
		assertEquals("myState", m.getStates().get(0).getName());
		assertEquals("source", m.getStates().get(0).getSide());
	}

	@Test
	void batch_forward_test_parses() throws Exception {
		BxModel m = parseHelper.parse("""
				problem P {
				  source metamodel "a.ecore"
				  target metamodel "b.ecore"
				}
				state s1 : P.source { Root {} }
				state t1 : P.target { Root {} }
				suite S for P {
				  test myTest {
				    category : batch.forward
				    edit source { op1 }
				    postcondition { source : s1, target : t1 }
				  }
				}
				""");
		assertNotNull(m);
		assertEquals(1, m.getSuites().size());
		var test = m.getSuites().get(0).getTests().get(0);
		assertInstanceOf(BatchFwd.class, test.getCategory());
	}

	@Test
	void concurrent_test_with_any_postcondition_parses() throws Exception {
		BxModel m = parseHelper.parse("""
				problem P {
				  source metamodel "a.ecore"
				  target metamodel "b.ecore"
				}
				state s1 : P.source { Root {} }
				state t1 : P.target { Root {} }
				state s2 : P.source { Root {} }
				state t2 : P.target { Root {} }
				suite S for P {
				  test concTest {
				    category : concurrent
				    edit source { op1 }
				    edit target { op2 }
				    postcondition any {
				      { source : s1, target : t1 }
				      { source : s2, target : t2 }
				    }
				  }
				}
				""");
		assertNotNull(m);
		var test = m.getSuites().get(0).getTests().get(0);
		assertInstanceOf(Concurrent.class, test.getCategory());
		assertInstanceOf(AnyPostcondition.class, test.getPostcondition());
		assertEquals(2, ((AnyPostcondition) test.getPostcondition()).getAlternatives().size());
	}

	@Test
	void problem_with_decisions_parses() throws Exception {
		BxModel m = parseHelper.parse("""
				problem P {
				  source metamodel "a.ecore"
				  target metamodel "b.ecore"
				  decisions { PREFER_PARENT, PREFER_EXISTING }
				}
				""");
		assertNotNull(m);
		assertEquals(2, m.getProblems().get(0).getDecisions().size());
	}
}
