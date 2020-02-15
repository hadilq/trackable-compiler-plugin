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

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import java.util.ArrayList

/**
 * A [SyntheticResolveExtension] that create descriptor of property of [propertyName].
 */
class TrackableSyntheticResolveExtension(
    private val propertyName: Name,
    private val fqTrackableAnnotation: FqName
) : SyntheticResolveExtension {

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        super.generateSyntheticProperties(thisDescriptor, name, bindingContext, fromSupertypes, result)

        if (thisDescriptor.isTrackable(fqTrackableAnnotation)) {
            result += PropertyDescriptorImpl.create(
                thisDescriptor,
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
            ).apply {
                initialize(getterDescriptor(this), null)
                setType(thisDescriptor.builtIns.stringType, emptyList(), thisDescriptor.thisAsReceiverParameter, null)
            }
        }
    }
}

private fun getterDescriptor(propertyDescriptor: PropertyDescriptor) =
    PropertyGetterDescriptorImpl(
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
    ).apply {
        initialize(propertyDescriptor.builtIns.stringType)
    }
