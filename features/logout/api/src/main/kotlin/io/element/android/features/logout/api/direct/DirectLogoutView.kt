/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.api.direct

import androidx.compose.runtime.Composable

/**
 * Renders the confirmation dialogs of logging out directly, in particular the warning shown when the user would lose their room keys.
 *
 * It draws nothing until the host screen sends a logout event.
 */
fun interface DirectLogoutView {
    /**
     * Draws whichever dialog the current state calls for.
     *
     * @param state the state produced by the direct logout presenter.
     */
    @Composable
    fun Render(state: DirectLogoutState)
}
