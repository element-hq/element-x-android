/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.google.common.truth.Truth.assertThat
import com.lemonappdev.konsist.api.Konsist
import org.junit.Test

class KonsistConfigTest {
    @Test
    fun `assert that Konsist detect all the project classes`() {
        assertThat(
            Konsist
                .scopeFromProject()
                .classes()
                .size
        )
            .isGreaterThan(1_000)
    }

    @Test
    fun `assert that Konsist detect all the test classes`() {
        assertThat(
            Konsist
                .scopeFromTest()
                .classes()
                .size
        )
            .isGreaterThan(100)
    }
}
