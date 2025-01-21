/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.anvilcodegen

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.anvilannotations.ContributesNode
import org.jetbrains.kotlin.name.FqName

class ContributesNodeProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val config: Config,
) : SymbolProcessor {
    data class Config(
        val enableLogging: Boolean = false,
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(ContributesNode::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val (validSymbols, invalidSymbols) = annotatedSymbols.partition { it.validate() }

        if (validSymbols.isEmpty()) return invalidSymbols

        for (ksClass in validSymbols) {
            if (config.enableLogging) {
                logger.warn("Processing ${ksClass.qualifiedName?.asString()}")
            }
            generateModule(ksClass)
            generateFactory(ksClass)
        }

        return invalidSymbols
    }

    private fun generateModule(ksClass: KSClassDeclaration) {
        val annotation = ksClass.annotations.find { it.shortName.asString() == "ContributesNode" }!!
        val scope = annotation.arguments.find { it.name?.asString() == "scope" }!!.value as KSType
        val modulePackage = ksClass.packageName.asString()
        val moduleClassName = "${ksClass.simpleName.asString()}_Module"
        val content = FileSpec.builder(
            packageName = modulePackage,
            fileName = moduleClassName,
        )
            .addType(
                TypeSpec.classBuilder(moduleClassName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(Module::class)
                    .addAnnotation(AnnotationSpec.builder(ContributesTo::class).addMember("%T::class", scope.toTypeName()).build())
                    .addFunction(
                        FunSpec.builder("bind${ksClass.simpleName.asString()}Factory")
                            .addModifiers(KModifier.ABSTRACT)
                            .addParameter("factory", ClassName(modulePackage, "${ksClass.simpleName.asString()}_AssistedFactory"))
                            .returns(ClassName.bestGuess(assistedNodeFactoryFqName.asString()).parameterizedBy(STAR))
                            .addAnnotation(Binds::class)
                            .addAnnotation(IntoMap::class)
                            .addAnnotation(
                                AnnotationSpec.Companion.builder(ClassName.bestGuess(nodeKeyFqName.asString())).addMember(
                                    "%T::class",
                                    ClassName.bestGuess(ksClass.qualifiedName!!.asString())
                                ).build()
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

        content.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                ksClass.containingFile!!
            ),
        )
    }

    @OptIn(KspExperimental::class)
    private fun generateFactory(ksClass: KSClassDeclaration) {
        val generatedPackage = ksClass.packageName.asString()
        val assistedFactoryClassName = "${ksClass.simpleName.asString()}_AssistedFactory"
        val constructor = ksClass.getConstructors().singleOrNull { it.isAnnotationPresent(AssistedInject::class) }
        val assistedParameters = constructor?.parameters?.filter { it.isAnnotationPresent(Assisted::class) }.orEmpty()
        if (constructor == null || assistedParameters.size != 2) {
            error(
                "${ksClass.qualifiedName} must have an @AssistedInject constructor with 2 @Assisted parameters",
            )
        }
        val contextAssistedParam = assistedParameters[0]
        if (contextAssistedParam.name?.asString() != "buildContext") {
            error(
                "${ksClass.qualifiedName} @Assisted parameter must be named buildContext",
            )
        }
        val pluginsAssistedParam = assistedParameters[1]
        if (pluginsAssistedParam.name?.asString() != "plugins") {
            error(
                "${ksClass.qualifiedName} @Assisted parameter must be named plugins",
            )
        }

        val nodeClassName = ClassName.bestGuess(ksClass.qualifiedName!!.asString())
        val buildContextClassName = contextAssistedParam.type.toTypeName()
        val pluginsClassName = pluginsAssistedParam.type.toTypeName()
        val content = FileSpec.builder(generatedPackage, assistedFactoryClassName)
            .addType(
                TypeSpec.interfaceBuilder(assistedFactoryClassName)
                    .addSuperinterface(ClassName.bestGuess(assistedNodeFactoryFqName.asString()).parameterizedBy(nodeClassName))
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
            .build()

        content.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                ksClass.containingFile!!
            ),
        )
    }

    companion object {
        private val assistedNodeFactoryFqName = FqName("io.element.android.libraries.architecture.AssistedNodeFactory")
        private val nodeKeyFqName = FqName("io.element.android.libraries.architecture.NodeKey")
    }
}
