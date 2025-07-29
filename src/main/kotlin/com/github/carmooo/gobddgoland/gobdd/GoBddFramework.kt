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

val GO_BDD_PATTERN = Regex("github\\.com/PaddleHQ/go-bdd/v\\d+")

class GoBddFramework : GoTestFramework() {
    override fun getName(): String {
        return "go-bdd"
    }

    override fun isAvailable(p0: com.intellij.openapi.module.Module?): Boolean {
        return true
    }

    override fun isAvailableOnFile(psiFile: PsiFile?): Boolean {
        if (psiFile == null || psiFile !is GoFile) {
            return false
        }
        
        val fileName = psiFile.name
        if (fileName != "bdd_test.go") {
            return false
        }
        
        return psiFile.imports.any { it.path?.let { path -> GO_BDD_PATTERN.matches(path) } == true }
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