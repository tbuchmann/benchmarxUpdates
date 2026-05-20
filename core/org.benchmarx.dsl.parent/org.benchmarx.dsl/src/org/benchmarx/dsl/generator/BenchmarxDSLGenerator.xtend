package org.benchmarx.dsl.generator

import com.google.inject.Inject
import java.nio.file.Paths
import org.benchmarx.dsl.StateSerializer
import org.benchmarx.dsl.benchmarxDSL.BatchBwd
import org.benchmarx.dsl.benchmarxDSL.BatchFwd
import org.benchmarx.dsl.benchmarxDSL.BxModel
import org.benchmarx.dsl.benchmarxDSL.Concurrent
import org.benchmarx.dsl.benchmarxDSL.IncrBwd
import org.benchmarx.dsl.benchmarxDSL.IncrFwd
import org.benchmarx.dsl.benchmarxDSL.ProblemDecl
import org.benchmarx.dsl.benchmarxDSL.Roundtrip
import org.benchmarx.dsl.benchmarxDSL.StateDecl
import org.benchmarx.dsl.benchmarxDSL.SuiteDecl
import org.benchmarx.dsl.generator.templates.BatchBackwardTemplate
import org.benchmarx.dsl.generator.templates.BatchForwardTemplate
import org.benchmarx.dsl.generator.templates.ComparatorStubTemplate
import org.benchmarx.dsl.generator.templates.ConcurrentTemplate
import org.benchmarx.dsl.generator.templates.DecisionsTemplate
import org.benchmarx.dsl.generator.templates.HelperStubTemplate
import org.benchmarx.dsl.generator.templates.IncrementalBackwardTemplate
import org.benchmarx.dsl.generator.templates.IncrementalForwardTemplate
import org.benchmarx.dsl.generator.templates.ParameterResolverTemplate
import org.benchmarx.dsl.generator.templates.PomTemplate
import org.benchmarx.dsl.generator.templates.RoundtripTemplate
import org.benchmarx.dsl.generator.templates.TestCaseTemplate
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

class BenchmarxDSLGenerator extends AbstractGenerator {

	@Inject extension TestCaseTemplate
	@Inject extension ParameterResolverTemplate
	@Inject extension DecisionsTemplate
	@Inject extension BatchForwardTemplate
	@Inject extension BatchBackwardTemplate
	@Inject extension IncrementalForwardTemplate
	@Inject extension IncrementalBackwardTemplate
	@Inject extension RoundtripTemplate
	@Inject extension ConcurrentTemplate
	@Inject extension HelperStubTemplate
	@Inject extension ComparatorStubTemplate
	@Inject extension PomTemplate
	@Inject StateSerializer stateSerializer

	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val model = resource.contents.head as BxModel
		val skipFsa = new SkipExistingFsa(fsa)

		for (problem : model.problems) {
			val suite = model.suites.findFirst[it.problem == problem]

			// Always-regenerated Java sources
			fsa.generateFile(problem.javaPath("TestCase"),          problem.testCaseContent(suite))
			fsa.generateFile(problem.javaPath("ParameterResolver"), problem.paramResolverContent)
			if (!problem.decisions.empty)
				fsa.generateFile(problem.javaPath("Decisions"), problem.decisionsContent)

			// Category test classes (only for categories present in the suite)
			if (suite !== null) {
				if (suite.hasBatchFwd)   fsa.generateFile(problem.testPath("batch/fwd/BatchForward"),                       suite.batchFwdContent(problem))
				if (suite.hasBatchBwd)   fsa.generateFile(problem.testPath("batch/bwd/BatchBackward"),                      suite.batchBwdContent(problem))
				if (suite.hasIncrFwd)    fsa.generateFile(problem.testPath("alignment_based/fwd/IncrementalForward"),        suite.incrFwdContent(problem))
				if (suite.hasIncrBwd)    fsa.generateFile(problem.testPath("alignment_based/bwd/IncrementalBackward"),       suite.incrBwdContent(problem))
				if (suite.hasRoundtrip)  fsa.generateFile(problem.testPath("alignment_based/roundtrip/RoundtripTests"),      suite.roundtripContent(problem))
				if (suite.hasConcurrent) fsa.generateFile(problem.testPath("concurrent/ConcurrentTests"),                   suite.concurrentContent(problem))
			}

			// Stubs — written only if file does not exist yet
			skipFsa.generateFile(problem.helperPath("Src"),     problem.srcHelperStub(suite))
			skipFsa.generateFile(problem.helperPath("Trg"),     problem.trgHelperStub(suite))
			skipFsa.generateFile(problem.comparatorPath("Src"), problem.srcComparatorStub)
			skipFsa.generateFile(problem.comparatorPath("Trg"), problem.trgComparatorStub)

			fsa.generateFile(problem.name + "/pom.xml", problem.pomContent)
		}

		// XMI fixtures from state blocks
		for (state : model.states) {
			try {
				val ecorePath = state.resolveEcorePath(resource)
				val xmi = stateSerializer.serializeToString(state, ecorePath)
				fsa.generateFile("resources/" + state.name + ".xmi", xmi)
			} catch (Exception e) {
				// .ecore not resolvable at generation time; skipping XMI for «state.name»
			}
		}
	}

	// ── Category presence checks ──────────────────────────────────────────

	def package hasBatchFwd(SuiteDecl s)   { s.tests.exists[category instanceof BatchFwd] }
	def package hasBatchBwd(SuiteDecl s)   { s.tests.exists[category instanceof BatchBwd] }
	def package hasIncrFwd(SuiteDecl s)    { s.tests.exists[category instanceof IncrFwd] }
	def package hasIncrBwd(SuiteDecl s)    { s.tests.exists[category instanceof IncrBwd] }
	def package hasRoundtrip(SuiteDecl s)  { s.tests.exists[category instanceof Roundtrip] }
	def package hasConcurrent(SuiteDecl s) { s.tests.exists[category instanceof Concurrent] }

	// ── Path helpers ──────────────────────────────────────────────────────

	def private javaPath(ProblemDecl p, String suffix) {
		"org/benchmarx/examples/" + p.name.toLowerCase + "/testsuite/" + p.name + suffix + ".java"
	}

	def private testPath(ProblemDecl p, String classRelPath) {
		"org/benchmarx/examples/" + p.name.toLowerCase + "/testsuite/" + classRelPath + ".java"
	}

	def private helperPath(ProblemDecl p, String side) {
		"org/benchmarx/examples/" + p.name.toLowerCase + "/helpers/" + p.name + side + "Helper.java"
	}

	def private comparatorPath(ProblemDecl p, String side) {
		"org/benchmarx/examples/" + p.name.toLowerCase + "/comparator/" + p.name + side + "Comparator.java"
	}

	def private resolveEcorePath(StateDecl state, Resource resource) {
		val ecoreRelPath = if (state.side == "source")
			state.metamodel.sourceMeta.path
		else
			state.metamodel.targetMeta.path
		val resourceUri = resource.URI
		if (resourceUri.isPlatformResource) {
			val project = ResourcesPlugin.workspace.root
				.getProject(resourceUri.segment(1))
			val file = project.getFile(new Path(ecoreRelPath))
			Paths.get(file.location.toOSString)
		} else {
			Paths.get(resourceUri.trimSegments(1).appendSegment(ecoreRelPath).toFileString)
		}
	}
}
