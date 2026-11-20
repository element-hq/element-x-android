/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Renders a few messages the way the timeline does, so that a screen changing how the timeline looks can preview the result. */
interface TimelineSampleRenderer {
    /**
     * @param textSizeSp the size in sp to render the message text at.
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(textSizeSp: Int, modifier: Modifier)
}
