/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.runtime.Composable
import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutModifier
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withTopLevel
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.ext.list.withoutNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutReceiverType
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistComposableTest {
    @Test
    fun `Top level function with '@Composable' annotation starting with a upper case should be placed in a file with the same name`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withTopLevel()
            .withoutModifier(KoModifier.PRIVATE)
            .withoutNameEndingWith("Preview")
            .withAllAnnotationsOf(Composable::class)
            .withoutReceiverType()
            .withoutName(
                // Add some exceptions...
                "InvisibleButton",
                "OutlinedButton",
                "SimpleAlertDialogContent",
                "TextButton",
                "AvatarColorsPreviewLight",
                "AvatarColorsPreviewDark",
                "IconsCompoundPreviewLight",
                "IconsCompoundPreviewRtl",
                "IconsCompoundPreviewDark",
                "CompoundSemanticColorsLight",
                "CompoundSemanticColorsLightHc",
                "CompoundSemanticColorsDark",
                "CompoundSemanticColorsDarkHc",
            )
            .assertTrue(
                additionalMessage =
                """
                Please check the filename. It should match the top level Composable function. If the filename is correct:
                - consider making the Composable private or moving it to its own file
                - at last resort, you can add an exception in the Konsist test
                """.trimIndent()
            ) {
                if (it.name.first().isLowerCase()) {
                    true
                } else {
                    val fileName = it.containingFile.name.removeSuffix(".kt")
                    fileName == it.name
                }
            }
    }
}
