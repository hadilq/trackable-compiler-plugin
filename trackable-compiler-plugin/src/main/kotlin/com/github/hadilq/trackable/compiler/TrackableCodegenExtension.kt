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

import org.jetbrains.kotlin.codegen.FunctionCodegen
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

/**
 * A compiler codegen extension that generates custom method for the classes with @Trackable annotation.
 */
open class TrackableCodegenExtension(
    private val getterName: Name
) : ExpressionCodegenExtension {

    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {
        val classBuilder = codegen.v
        val targetClass = codegen.myClass as? KtClass ?: return

        val container = codegen.descriptor
        if (container.kind != ClassKind.CLASS && container.kind != ClassKind.OBJECT) return

        val function = container.findGetter(getterName) ?: return
        val trackableGetter = function as? TrackableGetter ?: return

        val mv = classBuilder.newMethod(
            JvmDeclarationOrigin.NO_ORIGIN,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
            getterName.asString(),
            "()Ljava/lang/String;",
            null,
            null
        )
        mv.visitCode()
        val iv = InstructionAdapter(mv)

        iv.visitLdcInsn(trackableGetter.returnValue)
        iv.areturn(AsmTypes.JAVA_STRING_TYPE)

        FunctionCodegen.endVisit(mv, getterName.asString(), targetClass)
    }
}

private fun ClassDescriptor.findGetter(getterName: Name): FunctionDescriptor? =
    unsubstitutedMemberScope
        .getContributedFunctions(getterName, NoLookupLocation.FROM_BACKEND)
        .firstOrNull()
