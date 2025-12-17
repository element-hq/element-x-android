/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.constructors
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withSealedModifier
import com.lemonappdev.konsist.api.ext.list.parameters
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withoutConstructors
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.ext.list.withoutParents
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Assert.assertTrue
import org.junit.Test

class KonsistArchitectureTest {
    @Test
    fun `Data class state MUST not have default value`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("State")
            .withoutName(
                "CameraPositionState",
                "CustomSheetState",
            )
            .constructors
            .parameters
            .assertTrue { parameterDeclaration ->
                parameterDeclaration.defaultValue == null &&
                    // Using parameterDeclaration.defaultValue == null is not enough apparently,
                    // Also check that the text does not contain an equal sign
                    parameterDeclaration.text.contains("=").not()
            }
    }

    @Test
    fun `Events MUST be sealed interface`() {
        Konsist.scopeFromProject()
            .classes()
            .withSealedModifier()
            .withNameEndingWith("Events")
            .assertEmpty(additionalMessage = "Events class MUST be sealed interface")
    }

    @Test
    fun `Sealed class without constructor and without parent MUST be sealed interface`() {
        Konsist.scopeFromProject()
            .classes()
            .withSealedModifier()
            .withoutConstructors()
            .withoutParents()
            .assertEmpty(additionalMessage = "Sealed class without constructor MUST be sealed interface")
    }

    @Test
    fun `Sealed interface used in Composable MUST be Immutable or Stable`() {
        var failingTestFound = false
        // List all sealed interface without Immutable nor Stable annotation in the project
        val forbiddenInterfacesForComposableParameter = Konsist.scopeFromProject()
            .interfaces()
            .withSealedModifier()
            .withoutAnnotationOf(Immutable::class, Stable::class)
            .map { it.fullyQualifiedName }
        Konsist.scopeFromProject()
            .functions()
            .withAnnotationOf(Composable::class)
            .assertTrue(additionalMessage = "Consider adding the @Immutable or @Stable annotation to the sealed interface") {
                val result = it.parameters.all { param ->
                    val type = param.type.text
                    return@all if (type.startsWith("@") || type.contains("->") || type.startsWith("suspend")) {
                        true
                    } else {
                        val typePackage = param.type.sourceDeclaration?.let { declaration ->
                            declaration.asTypeParameterDeclaration()?.packagee
                                ?: declaration.asExternalDeclaration()?.packagee
                                ?: declaration.asClassOrInterfaceDeclaration()?.packagee
                                ?: declaration.asKotlinTypeDeclaration()?.packagee
                                ?: declaration.asObjectDeclaration()?.packagee
                        }?.name
                        if (typePackage == null) {
                            false
                        } else {
                            val fullyQualifiedName = "$typePackage.$type"
                            fullyQualifiedName !in forbiddenInterfacesForComposableParameter
                        }
                    }
                }
                if (!result && !failingTestFound && it.name == "FailingComposableWithNonImmutableSealedInterface") {
                    failingTestFound = true
                    true
                } else {
                    result
                }
            }
        assertTrue("FailingComposableWithNonImmutableSealedInterface should make this test fail.", failingTestFound)
    }
}
