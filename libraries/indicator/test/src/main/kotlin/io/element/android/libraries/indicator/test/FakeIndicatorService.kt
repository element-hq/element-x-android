/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.indicator.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.indicator.api.IndicatorService

class FakeIndicatorService : IndicatorService {
    private val showRoomListTopBarIndicatorResult: MutableState<Boolean> = mutableStateOf(false)
    private val showSettingChatBackupIndicatorResult: MutableState<Boolean> = mutableStateOf(false)

    fun setShowRoomListTopBarIndicator(value: Boolean) {
        showRoomListTopBarIndicatorResult.value = value
    }

    fun setShowSettingChatBackupIndicator(value: Boolean) {
        showSettingChatBackupIndicatorResult.value = value
    }

    @Composable
    override fun showRoomListTopBarIndicator(): State<Boolean> {
        return showRoomListTopBarIndicatorResult
    }

    @Composable
    override fun showSettingChatBackupIndicator(): State<Boolean> {
        return showSettingChatBackupIndicatorResult
    }
}
