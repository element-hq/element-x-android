/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.notifications.edit

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList

data class EditDefaultNotificationSettingState(
    val isOneToOne: Boolean,
    val mode: RoomNotificationMode?,
    val roomsWithUserDefinedMode: ImmutableList<RoomSummary.Filled>,
    val changeNotificationSettingAction: AsyncData<Unit>,
    val displayMentionsOnlyDisclaimer: Boolean,
    val eventSink: (EditDefaultNotificationSettingStateEvents) -> Unit,
)
