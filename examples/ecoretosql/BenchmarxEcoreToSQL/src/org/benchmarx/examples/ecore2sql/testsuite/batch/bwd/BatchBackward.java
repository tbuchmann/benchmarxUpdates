package org.benchmarx.examples.ecore2sql.testsuite.batch.bwd;

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
public class BatchBackward extends EcoreToSQLTestCase {
	public BatchBackward() {
		super();
	}
	
	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}
	
	/**
	 * <b>Test</b> for name change of an empty schema.<br/>
	 * <b>Expect</b> name, uri and prefix in the EPackage is also changed.<br/>
	 * <b>Features</b>: fwd, fixed
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testSQLNameChangeOfEmpty(BXTool<EPackage, Schema, Decisions> tool)
	{
		this.tool = tool;
		initialise();
		util.assertPrecondition("RootElementEcore", "RootElementSQL");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperSQL::changePackageName));
		//------------
		util.assertPostcondition("CompositeListPackageEcore", "CompositeListPackageSQL");
		terminate();
	}
	
	/**
	 * <b>Test</b> for creation of a simple SQL schema.
	 * <br/>
	 * <b>Expect</b> the creation of the corresponding ecore model.
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
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable));
		//------------
		util.assertPostcondition("CompositeListSimple-OperationsEcore", "CompositeListSimpleSQL");
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
		tool.performAndPropagateTargetEdit(trgEdit(
				helperSQL::changePackageName,
				helperSQL::createNodeTable,
				helperSQL::createLeafTable,
				helperSQL::createDataNodeTable,
				helperSQL::createListTable,
				helperSQL::createDataElementTable,
				helperSQL::createPairTable,
				helperSQL::createValueTable,
				helperSQL::createKeyTable,
				helperSQL::createKey_keyValuesTable,
				helperSQL::createList_start_inverse_Node_startOfTable,
				helperSQL::changeDataNodeTable,
				helperSQL::changeListTable));
		tool.performIdleSourceEdit(srcEdit(helperEcore::setDataElementAsInterface));
		//------------
		util.assertPostcondition("CompositeListData-OperationsEcore", "CompositeListDataSQL");
		terminate();
	}
}
