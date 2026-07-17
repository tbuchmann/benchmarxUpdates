package org.benchmarx.dag.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.benchmarx.dag.core.DagModelBuilder.Direction;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import dag.ArithmeticOperator;
import dag.Number;
import dag.Model;
import dag.Variable;

public class DagHelper {
	private DagModelBuilder builder;
	private Supplier<Model> rootSupplier;
	private BiConsumer<EAttribute, List<?>> changeAttribute;
	private Consumer<EObject> deleteNode;
	private BiConsumer<EReference, List<EObject>> deleteEdge;
	
	public DagHelper(Supplier<Model> rootSupplier, Consumer<EObject> createSourceNode,
			BiConsumer<EReference, List<EObject>> createSourceEdge,
			BiConsumer<EAttribute, List<?>> changeSourceAttribute, Consumer<EObject> deleteSourceNode,
			BiConsumer<EObject, List<EObject>> moveSourceNode, BiConsumer<EReference, List<EObject>> deleteSourceEdge) {
		builder = new DagModelBuilder(rootSupplier.get());
		this.rootSupplier = rootSupplier;
		this.changeAttribute = changeSourceAttribute;
		this.deleteEdge = deleteSourceEdge;
		this.deleteNode = deleteSourceNode;
	}
	
	public void create42() {
		builder.number(Direction.UP).setValue(42);
	}
	
	public void createTextSum() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD);
		builder.variable(Direction.LEFT).setName("Answer to the Ultimate Question of Life, The Universe, and Everything");
		builder.navigate(Direction.UP).variable(Direction.RIGHT).setName("Deep Thought");
		builder.navigateToRoot().variable(Direction.RIGHT).setName("7.5 million years");
	}
	
	public void createComplexNumberExample() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD);
		builder.number(Direction.LEFT).setValue(10).saveReference("10").navigate(Direction.UP);
		builder.number(Direction.RIGHT).setValue(1).saveReference("1").navigate(Direction.UP, Direction.UP);
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE).saveReference("2");
		builder.reference(Direction.LEFT, "10").navigate(Direction.UP);
		builder.number(Direction.RIGHT).setValue(5).saveReference("5").navigate(Direction.UP, Direction.UP, Direction.UP);
		builder.reference(Direction.RIGHT, "1").navigateToRoot();
		
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.SUBTRACT);
		builder.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY);
		builder.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD);
		builder.reference(Direction.RIGHT, "10").navigate(Direction.UP);
		builder.reference(Direction.LEFT, "1").navigate(Direction.UP, Direction.UP);
		builder.reference(Direction.LEFT, "2").setOp(ArithmeticOperator.DIVIDE).navigate(Direction.UP, Direction.UP);
		builder.reference(Direction.RIGHT, "1").navigateToRoot();
	}
	
	public void createMulitpleSubtrees() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.SUBTRACT)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD).saveReference("7")
					.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD).saveReference("2")
						.number(Direction.LEFT).setValue(1).saveReference("1").navigate(Direction.UP)
						.reference(Direction.RIGHT, "1").navigate(Direction.UP, Direction.UP)
					.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
						.reference(Direction.LEFT, "1").navigate(Direction.UP)
						.operator(Direction.RIGHT).setOp(ArithmeticOperator.ADD)
							.reference(Direction.LEFT, "2").navigate(Direction.UP)
							.reference(Direction.RIGHT, "2").navigateToRoot().navigate(Direction.LEFT)
				.reference(Direction.RIGHT, "7").navigateToRoot()
			.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.ADD)
					.reference(Direction.LEFT, "7").navigate(Direction.UP)
					.reference(Direction.RIGHT, "7").navigate(Direction.UP, Direction.UP)
				.reference(Direction.RIGHT, "2").navigateToRoot();
	}
	
	public void createBestDigit() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.SUBTRACT)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
				.number(Direction.LEFT).setValue(7).saveReference("7").navigate(Direction.UP)
				.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
			.reference(Direction.RIGHT, "7").navigateToRoot();
	}
	
	public void insertMoreBestDigits() { // in BestDigit
		builder.navigate(Direction.LEFT).saveReference("7*sieben").navigateToRoot();
		builder.navigate(Direction.RIGHT).delete().operator(Direction.RIGHT).setOp(ArithmeticOperator.SUBTRACT)
			.reference(Direction.LEFT, "7*sieben").navigate(Direction.UP)
			.variable(Direction.RIGHT).setName("zweiundvierzig").navigateToRoot();
	}
	
	public void createBestDigitRef() {
		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD)
			.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY)
					.number(Direction.LEFT).setValue(7).saveReference("7").navigate(Direction.UP)
					.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
				.reference(Direction.RIGHT, "7").navigate(Direction.UP, Direction.UP)
			.operator(Direction.RIGHT).setOp(ArithmeticOperator.MULTIPLY)
				.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
					.number(Direction.LEFT).setValue(14).saveReference("14").navigate(Direction.UP)
					.reference(Direction.RIGHT, "14").navigate(Direction.UP, Direction.UP)
				.operator(Direction.RIGHT).setOp(ArithmeticOperator.DIVIDE)
					.reference(Direction.LEFT, "7").navigate(Direction.UP)
					.number(Direction.RIGHT).setValue(2).navigateToRoot();
	}
	
	public void modifyBestDigitRef() { // in BestDigitRef
		builder.navigate(Direction.LEFT)
			.setOp(ArithmeticOperator.MULTIPLY)
				.navigate(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
					.navigate(Direction.LEFT).delete().number(Direction.LEFT).setValue(8).navigate(Direction.UP)
					.navigate(Direction.RIGHT).setName("zwei").navigate(Direction.UP, Direction.UP, Direction.UP)
			.navigate(Direction.RIGHT)
				.navigate(Direction.LEFT)
					.navigate(Direction.RIGHT).saveReference("14").navigate(Direction.UP, Direction.UP)
				.navigate(Direction.RIGHT).setOp(ArithmeticOperator.MULTIPLY)
					.navigate(Direction.LEFT).delete().reference(Direction.LEFT, "14").navigate(Direction.UP)
					.navigate(Direction.RIGHT).delete().reference(Direction.RIGHT, "14").navigateToRoot();
	}
	
	public void createMoreBestDigits() {
		createBestDigit();
		insertMoreBestDigits();
	}
	
	public void removeSomeBestDigits() { // in MoreBestDigits
		builder.navigate(Direction.LEFT).navigate(Direction.LEFT).saveReference("7").navigateToRoot();
		builder.navigate(Direction.RIGHT).delete().navigateToRoot();
		builder.reference(Direction.RIGHT, "7").navigateToRoot();
	}
	
	public void changeIncrementalID() {
		rootSupplier.get().getExprs().stream().forEach(e -> e.setIncrementalID("incrTestValue"));
	}
	
	public void changeIncrementalIDOf8() {
		rootSupplier.get().getExprs().stream().filter(Number.class::isInstance).map(Number.class::cast).filter(n -> n.getValue() == 8).forEach(n -> n.setIncrementalID("incrTestValue8"));
	}
	
//	public void createStillBestDigit() {
//		builder = new DagModelBuilder(model);
//		builder.operator(Direction.UP).setOp(ArithmeticOperator.ADD)
//			.operator(Direction.LEFT).setOp(ArithmeticOperator.SUBTRACT)
//				.operator(Direction.LEFT).setOp(ArithmeticOperator.MULTIPLY).saveReference("7*sieben")
//					.number(Direction.LEFT).setValue(7).navigate(Direction.UP)
//					.variable(Direction.RIGHT).setName("sieben").navigate(Direction.UP, Direction.UP)
//				.reference(Direction.RIGHT, "7*sieben").navigate(Direction.UP)
//			.variable(Direction.RIGHT).setName("zweiundvierzig").navigateToRoot();
//	}
	
	public void idleDelta() {
	}

	// Direct (non-navigation) mutation used for Conflicts tests: renames the shared "sieben"
	// variable independently of DagModelBuilder, so it's safe to call regardless of whether
	// this side's structure was authored by this helper's own builder or created via
	// propagation from the other side.
	public void renameSharedVariableSieben() {
		rootSupplier.get().getExprs().stream()
			.filter(Variable.class::isInstance)
			.map(Variable.class::cast)
			.filter(v -> "sieben".equals(v.getName()))
			.forEach(v -> v.setName("unbekannt"));
	}
}
