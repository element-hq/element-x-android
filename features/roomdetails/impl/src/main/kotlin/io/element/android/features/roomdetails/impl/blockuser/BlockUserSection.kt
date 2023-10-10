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

package io.element.android.features.roomdetails.impl.blockuser

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsEvents
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun BlockUserSection(state: RoomMemberDetailsState, modifier: Modifier = Modifier) {
    PreferenceCategory(showDivider = false, modifier = modifier) {
        when (state.isBlocked) {
            is Async.Failure -> PreferenceBlockUser(isBlocked = state.isBlocked.prevData, isLoading = false, eventSink = state.eventSink)
            is Async.Loading -> PreferenceBlockUser(isBlocked = state.isBlocked.prevData, isLoading = true, eventSink = state.eventSink)
            is Async.Success -> PreferenceBlockUser(isBlocked = state.isBlocked.data, isLoading = false, eventSink = state.eventSink)
            Async.Uninitialized -> PreferenceBlockUser(isBlocked = null, isLoading = true, eventSink = state.eventSink)
        }
    }
    if (state.isBlocked is Async.Failure) {
        RetryDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(RoomMemberDetailsEvents.ClearBlockUserError) },
            onRetry = {
                val event = when (state.isBlocked.prevData) {
                    true -> RoomMemberDetailsEvents.UnblockUser(needsConfirmation = false)
                    false -> RoomMemberDetailsEvents.BlockUser(needsConfirmation = false)
                    null -> /*Should not happen */ RoomMemberDetailsEvents.ClearBlockUserError
                }
                state.eventSink(event)
            },
        )
    }
}

@Composable
private fun PreferenceBlockUser(
    isBlocked: Boolean?,
    isLoading: Boolean,
    eventSink: (RoomMemberDetailsEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isBlocked.orFalse()) {
        PreferenceText(
            title = stringResource(R.string.screen_dm_details_unblock_user),
            icon = Icons.Outlined.Block,
            onClick = { if (!isLoading) eventSink(RoomMemberDetailsEvents.UnblockUser(needsConfirmation = true)) },
            loadingCurrentValue = isLoading,
            modifier = modifier,
        )
    } else {
        PreferenceText(
            title = stringResource(R.string.screen_dm_details_block_user),
            icon = Icons.Outlined.Block,
            tintColor = MaterialTheme.colorScheme.error,
            onClick = { if (!isLoading) eventSink(RoomMemberDetailsEvents.BlockUser(needsConfirmation = true)) },
            loadingCurrentValue = isLoading,
            modifier = modifier,
        )
    }
}
