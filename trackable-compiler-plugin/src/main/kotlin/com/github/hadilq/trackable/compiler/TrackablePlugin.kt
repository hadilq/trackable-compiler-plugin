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
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.extensions.impl.ExtensionPointImpl
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@AutoService(ComponentRegistrar::class)
class TrackableComponentRegistrar constructor() : ComponentRegistrar {

    private var testConfiguration: CompilerConfiguration? = null

    // No way to define options yet in compile testing
    // https://github.com/tschuchortdev/kotlin-compile-testing/issues/34
    @TestOnly
    internal constructor(
        trackableAnnotation: String,
        enabled: Boolean = true,
        propertyName: String = "track"
    ) : this() {
        testConfiguration = CompilerConfiguration().apply {
            put(KEY_ENABLED, enabled)
            put(KEY_PROPERTY_NAME, propertyName)
            put(KEY_TRACKABLE_ANNOTATION, trackableAnnotation)
        }
    }

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val actualConfiguration = testConfiguration ?: configuration
        if (actualConfiguration[KEY_ENABLED] == false) return

        val realMessageCollector = configuration.get(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        val messageCollector = testConfiguration?.get(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            realMessageCollector
        ) ?: realMessageCollector
        val propertyName = checkNotNull(actualConfiguration[KEY_PROPERTY_NAME])
        val trackableAnnotation = checkNotNull(actualConfiguration[KEY_TRACKABLE_ANNOTATION])
        val fqRedactedAnnotation = FqName(trackableAnnotation)

        ExpressionCodegenExtension.registerExtensionAsFirst(
            project,
            TrackableCodegenExtension(messageCollector, Name.identifier(propertyName), fqRedactedAnnotation)
        )

        SyntheticResolveExtension.registerExtensionAsFirst(
            project,
            TrackableSyntheticResolveExtension(Name.identifier(propertyName), fqRedactedAnnotation)
        )
    }
}

fun <T> ProjectExtensionDescriptor<T>.registerExtensionAsFirst(project: Project, extension: T) =
    Extensions.getArea(project)
        .getExtensionPoint(extensionPointName)
        .let { it as ExtensionPointImpl }
        .registerExtension(extension, project)

