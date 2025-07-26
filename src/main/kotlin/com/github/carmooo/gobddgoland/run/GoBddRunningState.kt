package com.github.carmooo.gobddgoland.run

import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.execution.testing.GoTestRunningState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module


class GoBddRunningState(env: ExecutionEnvironment, module: Module, configuration: GoTestRunConfiguration) :
    GoTestRunningState(env, module, configuration)