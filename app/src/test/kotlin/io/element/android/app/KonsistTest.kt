/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.app

import androidx.compose.runtime.Composable
import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.constructors
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.list.parameters
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withReturnType
import com.lemonappdev.konsist.api.ext.list.withTopLevel
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.ext.list.withoutNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import org.junit.Test

class KonsistTest {

    @Test
    fun `Classes extending 'Presenter' should have 'Presenter' suffix`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(Presenter::class)
            .assertTrue {
                it.name.endsWith("Presenter")
            }
    }

    @Test
    fun `Functions with '@PreviewsDayNight' annotation should have 'Preview' suffix`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .assertTrue {
                it.hasNameEndingWith("Preview") &&
                    it.hasNameEndingWith("LightPreview").not() &&
                    it.hasNameEndingWith("DarkPreview").not()
            }
    }

    @Test
    fun `Top level function with '@Composable' annotation starting with a upper case should be placed in a file with the same name`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withTopLevel()
            .withoutModifier(KoModifier.PRIVATE)
            .withoutNameEndingWith("Preview")
            .withAllAnnotationsOf(Composable::class)
            .withoutName(
                // Add some exceptions...
                "OutlinedButton",
                "TextButton",
                "SimpleAlertDialogContent",
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

    @Test
    fun `Data class state MUST not have default value`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("State")
            .withoutName(
                "CameraPositionState",
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
    fun `Function which creates Presenter in test MUST be named 'createPresenterName'`() {
        Konsist
            .scopeFromTest()
            .functions()
            .withReturnType { it.name.endsWith("Presenter") }
            .withoutOverrideModifier()
            .assertTrue { functionDeclaration ->
                functionDeclaration.name == "create${functionDeclaration.returnType?.name}"
            }
    }

    @Test
    fun `no field should have 'm' prefix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .properties()
            .assertFalse {
                val secondCharacterIsUppercase = it.name.getOrNull(1)?.isUpperCase() ?: false
                it.name.startsWith('m') && secondCharacterIsUppercase
            }
    }
}
