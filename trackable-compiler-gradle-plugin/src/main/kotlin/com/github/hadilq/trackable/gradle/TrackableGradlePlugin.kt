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

import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val DEFAULT_ANNOTATION = "com.github.hadilq.trackable.annotations.Trackable"
internal const val DEFAULT_GETTER_NAME = "track"
internal const val DEFAULT_TRACK_WITH = "trackWith"

class TrackableGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("trackable", TrackablePluginExtension::class.java)
    }
}

open class TrackablePluginExtension {
    var trackableAnnotation: String = DEFAULT_ANNOTATION
    var enabled: Boolean = true
    var getterName: String = DEFAULT_GETTER_NAME
    var trackWith: String = DEFAULT_TRACK_WITH
}
