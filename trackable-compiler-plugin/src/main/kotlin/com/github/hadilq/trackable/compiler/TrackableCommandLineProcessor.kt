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
internal const val PROPERTY_NAME = "propertyName"
internal const val TRACKABLE_ANNOTATION = "trackableAnnotation"

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(ENABLED)
internal val KEY_PROPERTY_NAME = CompilerConfigurationKey<String>(PROPERTY_NAME)
internal val KEY_TRACKABLE_ANNOTATION = CompilerConfigurationKey<String>(TRACKABLE_ANNOTATION)

@AutoService(CommandLineProcessor::class)
class RedactedCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "trackable-compiler-plugin"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(ENABLED, "<true | false>", "", required = true),
            CliOption(PROPERTY_NAME, "String", "", required = true),
            CliOption(TRACKABLE_ANNOTATION, "String", "", required = true)
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        ENABLED -> configuration.put(KEY_ENABLED, value.toBoolean())
        PROPERTY_NAME -> configuration.put(KEY_PROPERTY_NAME, value)
        TRACKABLE_ANNOTATION -> configuration.put(KEY_TRACKABLE_ANNOTATION, value)
        else -> error("Unknown plugin option: ${option.optionName}")
    }
}
