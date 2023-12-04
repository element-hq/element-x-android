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
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.list.withFunction
import com.lemonappdev.konsist.api.ext.list.withReturnType
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistTestTest {
    @Test
    fun `Classes name containing @Test must end with 'Test''`() {
        Konsist
            .scopeFromTest()
            .classes()
            .withFunction { it.hasAnnotationOf(Test::class) }
            .assertTrue { it.name.endsWith("Test") }
    }

    @Test
    fun `Function which creates Presenter in test MUST be named 'createPresenterName'`() {
        Konsist
            .scopeFromTest()
            .functions()
            .withReturnType { it.name.endsWith("Presenter") }
            .withoutOverrideModifier()
            .assertTrue(
                additionalMessage = "The function can also be named 'createPresenter'. To please Konsist in this case, just remove the return type."
            ) { functionDeclaration ->
                functionDeclaration.name == "create${functionDeclaration.returnType?.name}"
            }
    }

    @Test
    fun `assertion methods must be imported`() {
        Konsist
            .scopeFromTest()
            .functions()
            // Exclude self
            .withoutName("assertion methods must be imported")
            .assertFalse(
                additionalMessage = "Import methods from Truth, instead of using for instance Truth.assertThat(...)"
            ) { functionDeclaration ->
                functionDeclaration.text.contains("Truth.")
            }
    }

    @Test
    fun `use isFalse() instead of isEqualTo(false)`() {
        Konsist
            .scopeFromTest()
            .functions()
            // Exclude self
            .withoutName("use isFalse() instead of isEqualTo(false)")
            .assertFalse { functionDeclaration ->
                functionDeclaration.text.contains("isEqualTo(false)")
            }
    }

    @Test
    fun `use isTrue() instead of isEqualTo(true)`() {
        Konsist
            .scopeFromTest()
            .functions()
            // Exclude self
            .withoutName("use isTrue() instead of isEqualTo(true)")
            .assertFalse { functionDeclaration ->
                functionDeclaration.text.contains("isEqualTo(true)")
            }
    }

    @Test
    fun `use isEmpty() instead of isEqualTo(empty)`() {
        Konsist
            .scopeFromTest()
            .functions()
            // Exclude self
            .withoutName("use isEmpty() instead of isEqualTo(empty)")
            .assertFalse { functionDeclaration ->
                functionDeclaration.text.contains("isEqualTo(empty")
            }
    }

    @Test
    fun `use isNull() instead of isEqualTo(null)`() {
        Konsist
            .scopeFromTest()
            .functions()
            // Exclude self
            .withoutName("use isNull() instead of isEqualTo(null)")
            .assertFalse { functionDeclaration ->
                functionDeclaration.text.contains("isEqualTo(null)")
            }
    }
}
