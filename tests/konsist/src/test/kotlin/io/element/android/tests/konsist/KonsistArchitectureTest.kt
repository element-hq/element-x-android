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

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.constructors
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withSealedModifier
import com.lemonappdev.konsist.api.ext.list.parameters
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutName
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
}
