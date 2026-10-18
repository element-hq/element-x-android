/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the invite people UI, so that other screens can embed it without depending on its implementation.
 */
interface InvitePeopleRenderer {
    /**
     * Draws the invite list and its search field.
     *
     * @param state the state produced by [InvitePeoplePresenter].
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        state: InvitePeopleState,
        modifier: Modifier,
    )
}
