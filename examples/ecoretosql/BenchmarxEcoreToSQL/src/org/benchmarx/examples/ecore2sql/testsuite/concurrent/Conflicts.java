package org.benchmarx.examples.ecore2sql.testsuite.concurrent;

import java.util.Collection;
import java.util.Set;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;
import sql.Table;

/**
 * Concurrent conflict tests for Ecore-to-SQL: both sides rename the same attribute/column
 * incompatibly in the same concurrent step. {@code BXAgentEcore2SQL} hardcodes
 * {@code SyncConflictPolicy.TARGET_WINS}, so resolution is deterministic here (unlike
 * examples whose adapter exposes a {@code Configurator}): the target's name always wins
 * and is propagated back to the source.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends EcoreToSQLTestCase {

	private static final Set<String> DATA_ELEMENT_CLASSES = Set.of("DataElement", "Pair", "Value", "Key");

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}

	/**
	 * <b>Test</b> for a rename-vs-rename conflict on List.length: source renames the
	 * attribute to "count" while target independently renames the corresponding column
	 * to "size" in the same concurrent step (CF-RenameListLength).<br/>
	 * <b>Expect</b>: TARGET_WINS resolves the conflict; both sides end up named "size".<br/>
	 * <b>Features</b>: concurrent, conflict, rename-vs-rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentRenameListLengthConflict(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::createSimpleCompositeList, helperEcore::changePackageName));
		util.assertPrecondition("MonotonicCreatingPreEcore", "MonotonicCreatingPreSQL");
		// Concurrent: SRC renames List.length to "count"; TRG independently renames the
		// same column to "size" - a genuine incompatible edit on the same attribute/column.
		tool.performAndPropagateEdit(
				srcEdit(helperEcore::renameListLengthAttribute),
				trgEdit(helperSQL::renameListLengthColumnToSize));
		util.assertPostcondition("ConflictsListLengthEcore", "ConflictsListLengthSQL");
		terminate();
	}

	/**
	 * <b>Test</b> MonotonicCreating: source concurrently adds new EClasses (DataElement,
	 * Pair, Key, Value) while target makes no change. After sync(), target must gain
	 * corresponding new Tables for each newly added EClass, and those Tables must be
	 * structurally complete (an id column and a primary key at minimum - every real Table
	 * produced by the forward transform has both).<br/>
	 * <b>Expect</b>: one Table per EClass, each with an id column and a primary key.<br/>
	 * <b>Features</b>: concurrent, ctm-create, sync
	 *
	 * <p>Previously reproduced a confirmed BXAgent bug: {@code sync()} created a Table
	 * per new EClass via {@code createAndMapCTMObjectsIncremental}, but only as an empty
	 * shell (name + annotation, no id column/primary key/columns). Fixed upstream in the
	 * bxagent generator repo 2026-07-23; see {@code BXAgent-KnownIssues.md} #3 for the
	 * history.</p>
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testMonotonicCreating(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::createSimpleCompositeList, helperEcore::changePackageName));
		util.assertPrecondition("MonotonicCreatingPreEcore", "MonotonicCreatingPreSQL");
		// Concurrent: source adds DataElement feature, target is idle
		tool.performAndPropagateEdit(
				srcEdit(helperEcore::addDataElementFeature, helperEcore::changeListAddParameter),
				trgEdit(helperSQL::idleDelta));
		// Assert: one Table-with-class-annotation per EClass
		EPackage src = tool.getSourceModel();
		Schema tgt = tool.getTargetModel();
		long srcClasses = src.getEClassifiers().stream().filter(c -> c instanceof EClass).count();
		long tgtClassTables = tgt.getOwnedTables().stream()
				.filter(t -> t.getOwnedAnnotations().stream().anyMatch(a -> "class".equals(a.getAnnotation())))
				.count();
		Assertions.assertEquals(srcClasses, tgtClassTables,
				"After MonotonicCreating sync: expected one class-Table per EClass");
		// Assert: each newly created Table is structurally complete, not an empty stub
		assertClassTablesStructurallyComplete(tgt);
		terminate();
	}

	/**
	 * <b>Test</b> MonotonicDeleting: source concurrently deletes the DataElement feature
	 * set (DataElement, Pair, Key, Value EClasses) while target makes no change.
	 * After sync(), the corresponding Tables must be removed from target, and the
	 * "EObject" root table must not be left with orphaned identity columns/foreign keys
	 * for the deleted classes.<br/>
	 * <b>Expect</b>: #Tables-with-class-annotation == #EClasses in source (4); EObject has
	 * no leftover DataElement/Pair/Value/Key columns.<br/>
	 * <b>Features</b>: concurrent, ctm-delete, sync
	 *
	 * <p>Previously reproduced a confirmed BXAgent bug: {@code sync()} removed the
	 * DataElement/Pair/Value/Key Tables but left their "unique" identity columns and
	 * foreign keys behind on the "EObject" root table - referential corruption, not just
	 * an incomplete propagation. Fixed upstream in the bxagent generator repo 2026-07-23;
	 * see {@code BXAgent-KnownIssues.md} #3 for the history.</p>
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testMonotonicDeleting(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperEcore::createSimpleCompositeList,
				helperEcore::changePackageName,
				helperEcore::addDataElementFeature,
				helperEcore::changeListAddParameter));
		util.assertPrecondition("MonotonicDeletingPreEcore", "MonotonicDeletingPreSQL");
		// Concurrent: source deletes DataElement feature, target is idle
		tool.performAndPropagateEdit(
				srcEdit(helperEcore::deleteDataElementFeature),
				trgEdit(helperSQL::idleDelta));
		// Assert: class-Tables removed for deleted EClasses
		EPackage src = tool.getSourceModel();
		Schema tgt = tool.getTargetModel();
		long srcClasses = src.getEClassifiers().stream().filter(c -> c instanceof EClass).count();
		long tgtClassTables = tgt.getOwnedTables().stream()
				.filter(t -> t.getOwnedAnnotations().stream().anyMatch(a -> "class".equals(a.getAnnotation())))
				.count();
		Assertions.assertEquals(srcClasses, tgtClassTables,
				"After MonotonicDeleting sync: expected class-Tables to match remaining EClasses");
		// Assert: EObject has no leftover identity columns/foreign keys for the deleted classes
		assertNoOrphanedIdentityColumns(tgt);
		terminate();
	}

	/**
	 * <b>Test</b> NonMonotonic: source adds new EClasses while target independently
	 * renames List.length column to "size" (non-conflicting concurrent edits).
	 * After sync(), new Tables appear AND the attribute rename is propagated back.<br/>
	 * <b>Expect</b>: new, structurally complete class-Tables created; List.length renamed
	 * to "size" in source.<br/>
	 * <b>Features</b>: concurrent, ctm-create, rename, non-conflicting
	 *
	 * <p>Previously shared the creation half of {@link #testMonotonicCreating}'s bug
	 * (empty Table stubs for new EClasses); the rename-propagation half always worked on
	 * its own (see {@link #testConcurrentRenameListLengthConflict}). Fixed upstream in the
	 * bxagent generator repo 2026-07-23; see {@code BXAgent-KnownIssues.md} #3 for the
	 * history.</p>
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testNonMonotonic(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::createSimpleCompositeList, helperEcore::changePackageName));
		util.assertPrecondition("MonotonicCreatingPreEcore", "MonotonicCreatingPreSQL");
		// Concurrent: source adds DataElement; target renames List.length → "size"
		tool.performAndPropagateEdit(
				srcEdit(helperEcore::addDataElementFeature, helperEcore::changeListAddParameter),
				trgEdit(helperSQL::renameListLengthColumnToSize));
		// Assert: new Tables created for added EClasses, and structurally complete
		EPackage src = tool.getSourceModel();
		Schema tgt = tool.getTargetModel();
		long srcClasses = src.getEClassifiers().stream().filter(c -> c instanceof EClass).count();
		long tgtClassTables = tgt.getOwnedTables().stream()
				.filter(t -> t.getOwnedAnnotations().stream().anyMatch(a -> "class".equals(a.getAnnotation())))
				.count();
		Assertions.assertEquals(srcClasses, tgtClassTables,
				"After NonMonotonic sync: expected class-Tables for all EClasses");
		assertClassTablesStructurallyComplete(tgt);
		// Assert: target rename propagated back — List.length renamed to "size" in source
		boolean lengthRenamed = src.getEClassifiers().stream()
				.filter(c -> c instanceof EClass && "List".equals(c.getName()))
				.flatMap(c -> ((EClass) c).getEStructuralFeatures().stream())
				.anyMatch(f -> "size".equals(f.getName()));
		Assertions.assertTrue(lengthRenamed,
				"After NonMonotonic sync: target rename of length→size must propagate to source");
		terminate();
	}

	/**
	 * Every real Table produced by the forward transform has an id column and a primary
	 * key, even abstract ones (e.g. "Node"/"DataElement" both get id+PK despite being
	 * abstract superclasses). A Table missing either is a stub sync() didn't finish
	 * building.
	 */
	private static void assertClassTablesStructurallyComplete(Schema tgt) {
		for (String name : DATA_ELEMENT_CLASSES) {
			Table t = tgt.getOwnedTables().stream().filter(tb -> name.equals(tb.getName())).findFirst()
					.orElseThrow(() -> new AssertionError("Expected Table \"" + name + "\" to exist after sync"));
			Assertions.assertTrue(t.getOwnedColumns().stream().anyMatch(c -> "id".equals(c.getName())),
					"Table \"" + name + "\" is missing its id column (sync() created an empty stub)");
			Assertions.assertNotNull(t.getOwnedPrimaryKey(),
					"Table \"" + name + "\" is missing its primary key (sync() created an empty stub)");
		}
	}

	/**
	 * When a class is deleted, its "unique" identity column and foreign key on the
	 * "EObject" root table (added when the class's Table was originally created) must be
	 * removed along with it - otherwise EObject is left with dangling/corrupted foreign
	 * keys pointing at a Table that no longer exists.
	 */
	private static void assertNoOrphanedIdentityColumns(Schema tgt) {
		Table eObjectTable = tgt.getOwnedTables().stream().filter(t -> "EObject".equals(t.getName())).findFirst()
				.orElseThrow(() -> new AssertionError("Expected Table \"EObject\" to exist"));
		for (String name : DATA_ELEMENT_CLASSES) {
			Assertions.assertTrue(eObjectTable.getOwnedColumns().stream().noneMatch(c -> name.equals(c.getName())),
					"EObject still has an orphaned \"" + name + "\" identity column after the class was deleted");
		}
	}
}
