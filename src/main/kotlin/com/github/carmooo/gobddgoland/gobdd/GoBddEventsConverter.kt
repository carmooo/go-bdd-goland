package com.github.carmooo.gobddgoland.gobdd

import com.goide.execution.testing.GoTestEventsJsonConverter
import com.intellij.execution.testframework.TestConsoleProperties

class GoBddEventsConverter(s: String, consoleProperties: TestConsoleProperties) : GoTestEventsJsonConverter(
    "go-bdd", s, consoleProperties
)