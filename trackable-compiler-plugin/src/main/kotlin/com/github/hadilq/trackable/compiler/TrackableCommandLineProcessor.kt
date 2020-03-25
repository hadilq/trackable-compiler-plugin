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

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(ENABLED)

internal const val TRACKABLE_ANNOTATION = "com.github.hadilq.trackable.annotations.Trackable"
internal const val TRACK_PROPERTY_NAME = "track"

@AutoService(CommandLineProcessor::class)
class TrackableCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "trackable-compiler-plugin"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(ENABLED, "<true | false>", "", required = true)
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        ENABLED -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> error("Unknown plugin option: ${option.optionName}")
    }
}
