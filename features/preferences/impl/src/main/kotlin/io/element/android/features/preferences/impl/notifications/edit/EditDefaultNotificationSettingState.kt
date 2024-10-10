/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.ImmutableList

data class EditDefaultNotificationSettingState(
    val isOneToOne: Boolean,
    val mode: RoomNotificationMode?,
    val roomsWithUserDefinedMode: ImmutableList<EditNotificationSettingRoomInfo>,
    val changeNotificationSettingAction: AsyncAction<Unit>,
    val displayMentionsOnlyDisclaimer: Boolean,
    val eventSink: (EditDefaultNotificationSettingStateEvents) -> Unit,
)
