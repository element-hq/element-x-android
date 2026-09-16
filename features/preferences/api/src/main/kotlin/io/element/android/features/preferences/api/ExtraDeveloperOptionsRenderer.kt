/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Component for rendering extra developer options in the preferences screen.
 */
interface ExtraDeveloperOptionsRenderer {
    /**
     * Draws the extra options. The standard build binds a no-op implementation, so only the builds that actually add options provide a real one.
     *
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(modifier: Modifier)
}
