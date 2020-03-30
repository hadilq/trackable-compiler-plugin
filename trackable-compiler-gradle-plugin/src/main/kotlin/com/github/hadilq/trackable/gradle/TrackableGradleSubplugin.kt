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
package com.github.hadilq.trackable.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.internal.KaptVariantData
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal const val ENABLED = "enabled"
internal const val GETTER_NAME = "getterName"
internal const val TRACKABLE_ANNOTATION = "trackableAnnotation"

@AutoService(KotlinGradleSubplugin::class)
class TrackableGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean =
        project.plugins.hasPlugin(TrackableGradlePlugin::class.java)

    override fun getCompilerPluginId(): String = "trackable-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.github.hadilq.trackable",
            artifactId = "trackable-compiler-plugin",
            version = VERSION
        )

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension =
            project.extensions.findByType(TrackablePluginExtension::class.java) ?: TrackablePluginExtension()
        val annotation = extension.trackableAnnotation

        // Default annotation is used, so add it as a dependency
        if (annotation == DEFAULT_ANNOTATION) {
            project.dependencies.add(
                "implementation",
                "com.github.hadilq.trackable:trackable-compiler-plugin-annotations:$VERSION"
            )
        }

        val enabled = extension.enabled

        variantData?.apply {
            val variant = unwrapVariant(this)
            if (variant != null) {
                project.logger.debug("Resolving enabled status for android variant ${variant.name}")
            } else {
                project.logger.lifecycle("Unable to resolve variant type for $this. Falling back to default behavior of '$enabled'")
            }
        }

        return listOf(
            SubpluginOption(key = ENABLED, value = enabled.toString()),
            SubpluginOption(key = GETTER_NAME, value = extension.getterName),
            SubpluginOption(key = TRACKABLE_ANNOTATION, value = annotation)
        )
    }
}

private fun unwrapVariant(variantData: Any?): BaseVariant? {
    return when (variantData) {
        is BaseVariant -> {
            when (variantData) {
                is TestVariant -> variantData.testedVariant
                is UnitTestVariant -> variantData.testedVariant as? BaseVariant
                else -> variantData
            }
        }
        is KaptVariantData<*> -> unwrapVariant(variantData.variantData)
        else -> null
    }
}
