/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.indicator.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * A set of State<Boolean> to observe to display or not the indicators in the UI.
 */
interface IndicatorService {
    @Composable
    fun showRoomListTopBarIndicator(): State<Boolean>

    @Composable
    fun showSettingChatBackupIndicator(): State<Boolean>
}
