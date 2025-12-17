/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
