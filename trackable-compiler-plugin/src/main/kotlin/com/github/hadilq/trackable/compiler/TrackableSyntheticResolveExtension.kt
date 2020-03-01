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

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.incremental.record
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.lazy.LazyClassContext
import org.jetbrains.kotlin.resolve.lazy.declarations.ClassMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.source.getPsi
import java.util.ArrayList

/**
 * A [SyntheticResolveExtension] that create descriptor of property of [propertyName].
 */
class TrackableSyntheticResolveExtension(
    private val messageCollector: MessageCollector,
    private val propertyName: Name,
    private val fqTrackableAnnotation: FqName
) : SyntheticResolveExtension {

    private fun log(message: String) {
        messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "Trackable: $message",
            CompilerMessageLocation.create(null)
        )
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        if (!thisDescriptor.isTrackable() &&
            thisDescriptor.getAllSuperClassifiers().none { it.isTrackable() } &&
            thisDescriptor.getSuperInterfaces().none { it.isTrackable() }
        ) {
            log("Not trackable")
            return
        }
        if (thisDescriptor.isData || thisDescriptor.isInline) {
            log("data or inline class")
            val psi = thisDescriptor.source.getPsi()
            val location = MessageUtil.psiElementToMessageLocation(psi)
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                DATA_INLINE_CLASS_ERROR_MESSAGE,
                location
            )
            return
        }

        val returnValue: String =
            thisDescriptor.annotationTrackItWith(fqTrackableAnnotation) ?: thisDescriptor.name.asString()

        result += trackableProperty(
            thisDescriptor,
            propertyName,
            "get${propertyName.asString().capitalize()}",
            returnValue
        )
    }

    private fun Annotated.isTrackable(): Boolean =
        annotations.hasAnnotation(fqTrackableAnnotation)
}

private fun trackableProperty(
    thisDescriptor: ClassDescriptor,
    propertyName: Name,
    getterName: String,
    returnValue: String
) = object : TrackableProperty, PropertyDescriptorImpl(
    thisDescriptor,
    null,
    Annotations.EMPTY,
    Modality.FINAL,
    Visibilities.PUBLIC,
    false,
    propertyName,
    CallableMemberDescriptor.Kind.DECLARATION,
    thisDescriptor.source,
    false,
    false,
    false,
    false,
    false,
    false
) {
    override val propertyGetter: TrackableGetterProperty
        get() = getterDescriptor(this, getterName, returnValue)
}.apply {
    initialize(propertyGetter as PropertyGetterDescriptorImpl, null)
    setType(thisDescriptor.builtIns.stringType, emptyList(), thisDescriptor.thisAsReceiverParameter, null)
}

private fun getterDescriptor(
    propertyDescriptor: PropertyDescriptor,
    getterName: String,
    returnValue: String
) = object : TrackableGetterProperty, PropertyGetterDescriptorImpl(
    propertyDescriptor,
    Annotations.EMPTY,
    Modality.FINAL,
    Visibilities.PUBLIC,
    false,
    false,
    false,
    CallableMemberDescriptor.Kind.DECLARATION,
    null,
    propertyDescriptor.source
) {
    override val getterName: String
        get() = getterName
    override val returnValue: String
        get() = returnValue
}.apply {
    initialize(propertyDescriptor.builtIns.stringType)
}

internal fun DeclarationDescriptor.annotationTrackItWith(fqTrackableAnnotation: FqName): String? =
    annotations.trackableTrackName(fqTrackableAnnotation, ANNOTATION_TRACK_WITH)

internal fun Annotations.trackableTrackName(fqTrackableAnnotation: FqName, trackItWith: String): String? =
    findAnnotationConstantValue(fqTrackableAnnotation, trackItWith)

inline fun <reified R> Annotations.findAnnotationConstantValue(annotationFqName: FqName, property: String): R? =
    findAnnotation(annotationFqName)?.let { annotation ->
        annotation.allValueArguments.entries.singleOrNull { it.key.asString() == property }?.value?.value
    } as? R

interface TrackableProperty {
    val propertyGetter: TrackableGetterProperty
}

interface TrackableGetterProperty {
    val getterName: String
    val returnValue: String
}

const val DATA_INLINE_CLASS_ERROR_MESSAGE = "@Trackable is not supported on data and inline classes!"
const val ANNOTATION_TRACK_WITH = "trackWith"
