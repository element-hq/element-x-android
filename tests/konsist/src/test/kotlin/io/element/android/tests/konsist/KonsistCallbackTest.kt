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

class KonsistCallbackTest {
    @Test
    fun `we should not invoke Callback Input directly, we should use forEach`() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse {
                it.text.contains("callback?.")
            }
    }
}
