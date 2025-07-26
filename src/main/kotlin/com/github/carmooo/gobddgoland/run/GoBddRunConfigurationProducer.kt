package com.github.carmooo.gobddgoland.run

import com.github.carmooo.gobddgoland.gobdd.GoBddFramework
import com.goide.execution.GoBuildingRunConfiguration.Kind
import com.goide.execution.GoRunUtil
import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.execution.testing.GoTestRunConfigurationProducerBase
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.psi.GherkinFeature
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinScenario
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline
import org.jetbrains.plugins.cucumber.psi.GherkinTag

class GoBddRunConfigurationProducer protected constructor() :
    GoTestRunConfigurationProducerBase(GoBddFramework.INSTANCE) {
    override fun setupConfigurationFromContext(
        configuration: GoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<*>
    ): Boolean {
        val element = sourceElement.get() as PsiElement
        if (element.containingFile is GherkinFile) {
            configuration.testFramework = GoBddFramework.INSTANCE
            configuration.kind = Kind.PACKAGE
            configuration.goToolParams = GoRunUtil.filterOutInstallParameter(configuration.goToolParams)

            val scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario::class.java)
            val scenarioOutline = PsiTreeUtil.getParentOfType(element, GherkinScenarioOutline::class.java)
            val feature = PsiTreeUtil.getParentOfType(element, GherkinFeature::class.java)

            val availableTags = getAvailableTestTags(element.project)
            val selectedTag = findEarliestTag(scenario, scenarioOutline, feature, *availableTags.toTypedArray())

            var pattern = if (selectedTag != null) {
                val testFunctionName = getTestFunctionNameFromTag(selectedTag)
                "^\\Q${testFunctionName}\\E$"
            } else {
                // If multiple test functions exist but no tags match, don't provide a configuration
                if (availableTags.size > 1) {
                    return false
                }
                // Single test function or default fallback
                val defaultTestFunction = getDefaultTestFunctionName(availableTags)
                "^\\Q${defaultTestFunction}\\E$"
            }

            if (scenario != null) {
                val scenarioPattern = scenario.scenarioName.replace("/", "\\E/\\Q")
                pattern = pattern + "/^\\Q" + scenarioPattern + "\\E$"
            } else if (scenarioOutline != null) {
                val scenarioPattern = scenarioOutline.scenarioName.replace("/", "\\E/\\Q")
                pattern = pattern + "/^\\Q" + scenarioPattern + "\\E(#\\d+)?$"
            } else if (feature != null) {
                val allScenarioNames = getAllScenarioNamesFromFeature(feature)
                if (allScenarioNames.isNotEmpty()) {
                    val scenarioPatterns = allScenarioNames.map { scenarioName ->
                        val escaped = scenarioName.replace("/", "\\E/\\Q")
                        "$pattern/^\\Q${escaped}\\E$"
                    }
                    pattern = scenarioPatterns.joinToString("|")
                } else {
                    // Fallback to wildcard if no scenarios found
                    pattern = pattern + "/.*"
                }
            }

            pattern = pattern.replace(" ", "_")

            configuration.pattern = pattern

            configuration.setGeneratedName()
            return true
        }
        return false
    }
}

// Returns the earliest tag found, respecting priority order
private fun findEarliestTag(scenario: GherkinScenario?, scenarioOutline: GherkinScenarioOutline?, feature: GherkinFeature?, vararg tagNames: String): String? {
    // Priority 1: Scenario tags
    if (scenario != null) {
        val scenarioTag = findFirstTag(scenario, *tagNames)
        if (scenarioTag != null) return scenarioTag
    }

    // Priority 2: Scenario Outline tags
    if (scenarioOutline != null) {
        val outlineTag = findFirstTag(scenarioOutline, *tagNames)
        if (outlineTag != null) return outlineTag
    }

    // Priority 3: Feature tags
    feature?.let {
        val featureTag = findFirstTag(it, *tagNames)
        if (featureTag != null) return featureTag
    }

    return null
}

// Helper function to find the first matching tag in an element
private fun findFirstTag(element: PsiElement, vararg tagNames: String): String? {
    val tagTexts = when (element) {
        is GherkinFeature -> {
            // For features, tags are typically siblings that come before the feature
            val tags = mutableListOf<String>()
            var sibling = element.prevSibling
            while (sibling != null) {
                if (sibling is GherkinTag) {
                    tags.add(sibling.text)
                }
                sibling = sibling.prevSibling
            }
            tags.reversed() // Reverse to get original order
        }
        is GherkinScenario, is GherkinScenarioOutline -> {
            PsiTreeUtil.findChildrenOfType(element, GherkinTag::class.java)
                .map { it.text }
        }
        else -> emptyList()
    }

    // Find the first tag that matches any of our target tags (case-insensitive)
    for (tagText in tagTexts) {
        for (tagName in tagNames) {
            if (tagText.equals("@$tagName", ignoreCase = true)) {
                return tagName
            }
        }
    }

    return null
}

private fun getAllScenarioNamesFromFeature(feature: GherkinFeature): List<String> {
    val scenarioNames = mutableListOf<String>()

    val scenarios = PsiTreeUtil.findChildrenOfType(feature, GherkinScenario::class.java)
    scenarios.forEach { scenario ->
        scenario.scenarioName?.let { name ->
            if (name.isNotBlank()) {
                scenarioNames.add(name)
            }
        }
    }

    val scenarioOutlines = PsiTreeUtil.findChildrenOfType(feature, GherkinScenarioOutline::class.java)
    scenarioOutlines.forEach { outline ->
        outline.scenarioName?.let { name ->
            if (name.isNotBlank()) {
                scenarioNames.add(name)
            }
        }
    }

    return scenarioNames
}

// Get available test tags by discovering test functions from bdd_test.go
private fun getAvailableTestTags(project: com.intellij.openapi.project.Project): List<String> {
    val baseDir = project.baseDir ?: return emptyList()
    val bddTestFile = baseDir.findChild("bdd_test.go") ?: return emptyList()

    val bddTestPsi = com.intellij.psi.PsiManager.getInstance(project).findFile(bddTestFile) as? com.goide.psi.GoFile
        ?: return emptyList()

    val testFunctionPattern = Regex("Test(\\w+)Features")
    val tags = mutableListOf<String>()

    bddTestPsi.functions.forEach { function ->
        val functionName = function.name ?: return@forEach
        val matchResult = testFunctionPattern.find(functionName)
        if (matchResult != null) {
            val tag = matchResult.groupValues[1]
            tags.add(tag)
        }
    }

    return tags
}

private fun getTestFunctionNameFromTag(tag: String): String {
    return "Test${tag}Features"
}

private fun getDefaultTestFunctionName(availableTags: List<String>): String {
    return if (availableTags.size == 1) {
        getTestFunctionNameFromTag(availableTags.first())
    } else {
        "TestFeatures"
    }
}
