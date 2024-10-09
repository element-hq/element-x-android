/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.google.common.truth.Truth.assertThat
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withFunction
import com.lemonappdev.konsist.api.ext.list.withReturnType
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Ignore
import org.junit.Test

class KonsistTestTest {
    @Test
    fun `Ensure that unit tests are detected`() {
        val numberOfTests = Konsist
            .scopeFromTest()
            .functions()
            .withAnnotationOf(Test::class)
            .withoutAnnotationOf(Ignore::class)
            .size
        println("Number of unit tests: $numberOfTests")
        assertThat(numberOfTests).isGreaterThan(2000)
    }

    @Test
    fun `Classes name containing @Test must end with 'Test'`() {
        Konsist
            .scopeFromTest()
            .classes()
            .withoutName("S", "T")
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
