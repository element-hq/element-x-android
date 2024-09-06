/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistImportTest {
    @Test
    fun `Functions with '@VisibleForTesting' annotation should use 'androidx' version`() {
        Konsist
            .scopeFromProject()
            .imports
            .assertFalse(
                additionalMessage = "Please use 'androidx.annotation.VisibleForTesting' instead of " +
                    "'org.jetbrains.annotations.VisibleForTesting' (project convention).",
            ) {
                it.name == "org.jetbrains.annotations.VisibleForTesting"
            }
    }
}
