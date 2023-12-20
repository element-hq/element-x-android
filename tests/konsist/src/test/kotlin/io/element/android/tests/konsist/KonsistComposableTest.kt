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
}
