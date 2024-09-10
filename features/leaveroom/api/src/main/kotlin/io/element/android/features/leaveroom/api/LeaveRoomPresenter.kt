/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter

interface LeaveRoomPresenter : Presenter<LeaveRoomState> {
    @Composable
    override fun present(): LeaveRoomState
}
