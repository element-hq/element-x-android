/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.MaterialTextPreview
import org.junit.Rule
import org.junit.Test

class MaterialTextTest {
    // MaterialTextPreview relies on `weight`/`fillMaxSize`, so it needs a bounded frame to lay out.
    @get:Rule
    val paparazzi = createPaparazziRule(widthDp = 420, heightDp = 1200)

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "MaterialText Colors") {
            MaterialTextPreview()
        }
    }
}
