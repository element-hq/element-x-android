/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.root

import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

data class CreateRoomRootState(
    val isDebugBuild: Boolean,
    val applicationName: String,
    val userListState: UserListState,
    val startDmAction: AsyncAction<RoomId>,
    val eventSink: (CreateRoomRootEvents) -> Unit,
)
