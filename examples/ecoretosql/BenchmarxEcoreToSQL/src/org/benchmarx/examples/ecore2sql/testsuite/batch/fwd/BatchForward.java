package org.benchmarx.examples.ecore2sql.testsuite.batch.fwd;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

@ExtendWith(BXToolParameterResolver.class)
public class BatchForward extends EcoreToSQLTestCase {
	public BatchForward() {
		super();
	}
	
	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}
	
	/**
	 * <b>Test</b> for agreed upon starting state.<br/>
	 * <b>Expect</b> root elements of both source and target models.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testInitialiseSynchronisation(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		util.assertPostcondition("RootElementEcore", "RootElementSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for name change of an empty EPackage.<br/>
	 * <b>Expect</b> name in the SQL schema is also changed.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testEcoreNameChangeOfEmpty(BXTool<EPackage, Schema, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		util.assertPrecondition("RootElementEcore", "RootElementSQL");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::changePackageName));
		//------------
		util.assertPostcondition("CompositeListPackageEcore", "CompositeListPackageSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for creation of a simple ecore model.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding SQL schema.
	 * <br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateSimpleCompositeList(BXTool<EPackage, Schema, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changePackageName,
				helperEcore::createSimpleCompositeList));
		//------------
		util.assertPostcondition("CompositeListSimpleEcore", "CompositeListSimpleSQL");
		terminate();
	}

	/**
	 * Analogous to @link {@link #testCreateSimpleCompositeList()}, now with all possible reference types.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testCreateComplexCompositeList(BXTool<EPackage, Schema, Decisions> tool){
		this.tool = tool;
		initialise();
		// No precondition!
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::changePackageName,
				helperEcore::createSimpleCompositeList,
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		//------------
		util.assertPostcondition("CompositeListDataEcore", "CompositeListDataSQL");
		terminate();
	}
}
