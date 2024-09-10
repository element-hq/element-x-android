/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalAnvilApi::class)

package io.element.android.anvilcodegen

import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.fqName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.anvilannotations.ContributesNode
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * This is an anvil plugin that allows Node to use [ContributesNode] alone and let this plugin automatically
 * handle the rest of the Dagger wiring required for constructor injection.
 */
@AutoService(CodeGenerator::class)
class ContributesNodeCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(codeGenDir: File, module: ModuleDescriptor, projectFiles: Collection<KtFile>): Collection<GeneratedFile> {
        return projectFiles.classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(ContributesNode::class.fqName) }
            .flatMap { listOf(generateModule(it, codeGenDir, module), generateAssistedFactory(it, codeGenDir, module)) }
            .toList()
    }

    private fun generateModule(nodeClass: ClassReference.Psi, codeGenDir: File, module: ModuleDescriptor): GeneratedFile {
        val generatedPackage = nodeClass.packageFqName.toString()
        val moduleClassName = "${nodeClass.shortName}_Module"
        val scope = nodeClass.annotations.single { it.fqName == ContributesNode::class.fqName }.scope()
        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(
                TypeSpec.classBuilder(moduleClassName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(Module::class)
                    .addAnnotation(AnnotationSpec.builder(ContributesTo::class).addMember("%T::class", scope.asClassName()).build())
                    .addFunction(
                        FunSpec.builder("bind${nodeClass.shortName}Factory")
                            .addModifiers(KModifier.ABSTRACT)
                            .addParameter("factory", ClassName(generatedPackage, "${nodeClass.shortName}_AssistedFactory"))
                            .returns(assistedNodeFactoryFqName.asClassName(module).parameterizedBy(STAR))
                            .addAnnotation(Binds::class)
                            .addAnnotation(IntoMap::class)
                            .addAnnotation(
                                AnnotationSpec.Companion.builder(nodeKeyFqName.asClassName(module)).addMember(
                                    "%T::class",
                                    nodeClass.asClassName()
                                ).build()
                            )
                            .build(),
                    )
                    .build(),
            )
        }
        return createGeneratedFile(codeGenDir, generatedPackage, moduleClassName, content)
    }

    private fun generateAssistedFactory(nodeClass: ClassReference.Psi, codeGenDir: File, module: ModuleDescriptor): GeneratedFile {
        val generatedPackage = nodeClass.packageFqName.toString()
        val assistedFactoryClassName = "${nodeClass.shortName}_AssistedFactory"
        val constructor = nodeClass.constructors.singleOrNull { it.isAnnotatedWith(AssistedInject::class.fqName) }
        val assistedParameters = constructor?.parameters?.filter { it.isAnnotatedWith(Assisted::class.fqName) }.orEmpty()
        if (constructor == null || assistedParameters.size != 2) {
            throw AnvilCompilationException(
                "${nodeClass.fqName} must have an @AssistedInject constructor with 2 @Assisted parameters",
                element = nodeClass.clazz,
            )
        }
        val contextAssistedParam = assistedParameters[0]
        if (contextAssistedParam.name != "buildContext") {
            throw AnvilCompilationException(
                "${nodeClass.fqName} @Assisted parameter must be named buildContext",
                element = contextAssistedParam.parameter,
            )
        }
        val pluginsAssistedParam = assistedParameters[1]
        if (pluginsAssistedParam.name != "plugins") {
            throw AnvilCompilationException(
                "${nodeClass.fqName} @Assisted parameter must be named plugins",
                element = pluginsAssistedParam.parameter,
            )
        }

        val nodeClassName = nodeClass.asClassName()
        val buildContextClassName = contextAssistedParam.type().asTypeName()
        val pluginsClassName = pluginsAssistedParam.type().asTypeName()
        val content = FileSpec.buildFile(generatedPackage, assistedFactoryClassName) {
            addType(
                TypeSpec.interfaceBuilder(assistedFactoryClassName)
                    .addSuperinterface(assistedNodeFactoryFqName.asClassName(module).parameterizedBy(nodeClassName))
                    .addAnnotation(AssistedFactory::class)
                    .addFunction(
                        FunSpec.builder("create")
                            .addModifiers(KModifier.OVERRIDE, KModifier.ABSTRACT)
                            .addParameter("buildContext", buildContextClassName)
                            .addParameter("plugins", pluginsClassName)
                            .returns(nodeClassName)
                            .build(),
                    )
                    .build(),
            )
        }
        return createGeneratedFile(codeGenDir, generatedPackage, assistedFactoryClassName, content)
    }

    companion object {
        private val assistedNodeFactoryFqName = FqName("io.element.android.libraries.architecture.AssistedNodeFactory")
        private val nodeKeyFqName = FqName("io.element.android.libraries.architecture.NodeKey")
    }
}
