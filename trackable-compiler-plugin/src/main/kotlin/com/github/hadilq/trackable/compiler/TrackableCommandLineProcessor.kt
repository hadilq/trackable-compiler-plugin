/**
 * Copyright 2020 Hadi Lashkari Ghouchani

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hadilq.trackable.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal const val ENABLED = "enabled"
internal const val GETTER_NAME = "getterName"
internal const val TRACKABLE_ANNOTATION = "trackableAnnotation"
internal const val TRACK_WITH = "trackWith"

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(ENABLED)
internal val KEY_GETTER_NAME = CompilerConfigurationKey<String>(GETTER_NAME)
internal val KEY_TRACKABLE_ANNOTATION = CompilerConfigurationKey<String>(TRACKABLE_ANNOTATION)
internal val KEY_TRACK_WITH = CompilerConfigurationKey<String>(TRACK_WITH)

@AutoService(CommandLineProcessor::class)
class RedactedCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "trackable-compiler-plugin"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(ENABLED, "<true | false>", "", required = true),
            CliOption(GETTER_NAME, "String", "", required = true),
            CliOption(TRACKABLE_ANNOTATION, "String", "", required = true),
            CliOption(TRACK_WITH, "String", "", required = true)
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        ENABLED -> configuration.put(KEY_ENABLED, value.toBoolean())
        GETTER_NAME -> configuration.put(KEY_GETTER_NAME, value)
        TRACKABLE_ANNOTATION -> configuration.put(KEY_TRACKABLE_ANNOTATION, value)
        TRACK_WITH -> configuration.put(KEY_TRACK_WITH, value)
        else -> error("Unknown plugin option: ${option.optionName}")
    }
}
