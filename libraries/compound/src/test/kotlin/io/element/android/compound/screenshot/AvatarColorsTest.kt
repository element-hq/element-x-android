/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot

import io.element.android.compound.screenshot.utils.createPaparazziRule
import io.element.android.compound.theme.AvatarColorsPreviewDark
import io.element.android.compound.theme.AvatarColorsPreviewLight
import org.junit.Rule
import org.junit.Test

class AvatarColorsTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "Avatar Colors - Light") {
            AvatarColorsPreviewLight()
        }
        paparazzi.snapshot(name = "Avatar Colors - Dark") {
            AvatarColorsPreviewDark()
        }
    }
}
