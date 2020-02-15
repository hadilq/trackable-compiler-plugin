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

import org.jetbrains.annotations.NotNull
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.FunctionCodegen
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.context.FieldOwnerContext
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.WHEN_GET_ALL_DESCRIPTORS
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.diagnostics.OtherOrigin
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

/**
 * A compiler codegen extension that generates custom method for the classes with @Trackable annotation.
 */
class TrackableCodegenExtension(
    private val messageCollector: MessageCollector,
    private val propertyName: Name,
    private val fqTrackableAnnotation: FqName
) : ExpressionCodegenExtension {

    companion object {
        const val DATA_INLINE_CLASS_ERROR_MESSAGE = "@Trackable is not supported on data and inline classes!"
    }

    private fun log(message: String) {
        messageCollector.report(
            LOGGING,
            "Trackable: $message",
            CompilerMessageLocation.create(null)
        )
    }

    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {
        val targetClass = codegen.descriptor
        log("Reading ${targetClass.name}")

        if (!targetClass.isTrackable(fqTrackableAnnotation)) {
            log("Not trackable")
            return
        }
        if (targetClass.isData || targetClass.isInline) {
            log("data or inline class")
            val psi = codegen.descriptor.source.getPsi()
            val location = MessageUtil.psiElementToMessageLocation(psi)
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "@Trackable is not supported on data and inline classes!",
                location
            )
            return
        }

        PropertyGenerator(
            declaration = codegen.myClass as KtClassOrObject,
            classDescriptor = targetClass,
            fieldOwnerContext = codegen.context,
            v = codegen.v,
            generationState = codegen.state
        ).generateProperty(
            targetClass.findProperty(propertyName)
        )
    }
}

private class PropertyGenerator(
    private val declaration: KtClassOrObject,
    private val classDescriptor: ClassDescriptor,
    private val fieldOwnerContext: FieldOwnerContext<*>,
    private val v: ClassBuilder,
    generationState: GenerationState
) {

    private val typeMapper: KotlinTypeMapper = generationState.typeMapper

    private val access: Int = Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL

    private val toStringDesc: String
        get() = "()Ljava/lang/String;"

    fun generateProperty(property: PropertyDescriptor) {
        val methodOrigin = OtherOrigin(property)
        val getterDescriptor = property.getter!!
        val getterName = getterDescriptor.mapFunctionName()
        val mv = v.newMethod(methodOrigin, access, getterName, toStringDesc, null, null)

        mv.visitAnnotation(Type.getDescriptor(NotNull::class.java), false)?.visitEnd()

        val iv = InstructionAdapter(mv)

        mv.visitLdcInsn(classDescriptor.name.toString())
        iv.areturn(AsmTypes.JAVA_STRING_TYPE)

        FunctionCodegen.endVisit(mv, getterName, declaration)
    }

    private fun FunctionDescriptor.mapFunctionName(): String =
        typeMapper.mapFunctionName(this, fieldOwnerContext.contextKind)
}

private fun ClassDescriptor.findProperty(propertyName: Name): PropertyDescriptor =
    unsubstitutedMemberScope
        .getContributedVariables(propertyName, WHEN_GET_ALL_DESCRIPTORS)
        .first()

fun Annotated.isTrackable(trackableAnnotation: FqName): Boolean =
    annotations.hasAnnotation(trackableAnnotation)
