/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistMethodNameTest {
    @Test
    fun `Ensure that method name does not start or end with spaces`() {
        Konsist.scopeFromProject()
            .functions()
            .assertTrue {
                it.name.trim() == it.name
            }
    }
}
