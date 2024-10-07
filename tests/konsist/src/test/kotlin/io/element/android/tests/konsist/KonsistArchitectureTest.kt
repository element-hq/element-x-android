/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
                it.parameters.all { param ->
                    val type = param.type.text
                    return@all if (type.startsWith("@") || type.startsWith("(") || type.startsWith("suspend")) {
                        true
                    } else {
                        var typePackage = param.type.declaration.packagee?.name
                        if (typePackage == type) {
                            // Workaround, now that packagee.fullyQualifiedName is not available anymore
                            // It seems that when the type in in the same package as the function,
                            // the package is equal to the type (which is wrong).
                            // So in this case, use the package of the function
                            typePackage = it.packagee?.name
                        }
                        val fullyQualifiedName = "$typePackage.$type"
                        fullyQualifiedName !in forbiddenInterfacesForComposableParameter
                    }
                }
            }
    }
}
