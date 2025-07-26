package com.github.carmooo.gobddgoland.gobdd

import com.github.carmooo.gobddgoland.run.GoBddRunningState
import com.goide.GoFileType
import com.goide.execution.testing.GoTestFramework
import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.execution.testing.GoTestRunningState
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import kotlin.arrayOf

val PACKAGES = arrayOf(
    "github.com/PaddleHQ/go-bdd/v1",
    "github.com/PaddleHQ/go-bdd/v2",
    "github.com/PaddleHQ/go-bdd/v3",
    "github.com/PaddleHQ/go-bdd/v4",
)

class GoBddFramework : GoTestFramework() {
    override fun getName(): String {
        return "go-bdd"
    }

    override fun isAvailable(p0: com.intellij.openapi.module.Module?): Boolean {
        return true
    }

    override fun isAvailableOnFile(psiFile: PsiFile?): Boolean {
        if (psiFile == null || psiFile.fileType !== GoFileType.INSTANCE) {
            return false
        }
        
        // Look for bdd_test.go in the project root
        val project = psiFile.project
        val baseDir = project.baseDir ?: return false
        val bddTestFile = baseDir.findChild("bdd_test.go") ?: return false
        
        // Get the PSI file for bdd_test.go
        val bddTestPsi = com.intellij.psi.PsiManager.getInstance(project).findFile(bddTestFile)
        if (bddTestPsi !is GoFile) {
            return false
        }
        
        // Check if bdd_test.go has go-bdd imports
        return bddTestPsi.imports.any { it.path in PACKAGES }
    }

    override fun isAvailableOnFunction(goFunctionOrMethodDeclaration: GoFunctionOrMethodDeclaration?): Boolean {
        return false
    }

    override fun supportsJsonTestsOutput(): Boolean {
        return true
    }

    override fun newRunningState(
        executionEnvironment: ExecutionEnvironment,
        p1: Module,
        goTestRunConfiguration: GoTestRunConfiguration
    ): GoTestRunningState {
        return GoBddRunningState(executionEnvironment, p1, goTestRunConfiguration)
    }

    override fun createTestEventsConverter(
        s: String,
        testConsoleProperties: TestConsoleProperties,
        p2: com.intellij.openapi.module.Module?
    ): OutputToGeneralTestEventsConverter {
        return GoBddEventsConverter(s, testConsoleProperties)
    }

    override fun getPackageConfigurationName(packageName: String): String {
        return "go-bdd"
    }

    companion object {
        val INSTANCE: GoBddFramework = GoBddFramework()

        init {
            all().add(INSTANCE)
        }
    }
}