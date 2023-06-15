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
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsEvents
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.theme.LocalColors

@Composable
internal fun BlockUserSection(state: RoomMemberDetailsState, modifier: Modifier = Modifier) {
    PreferenceCategory(showDivider = false, modifier = modifier) {
        if (state.isBlocked) {
            PreferenceText(
                title = stringResource(R.string.screen_dm_details_unblock_user),
                icon = Icons.Outlined.Block,
                onClick = { state.eventSink(RoomMemberDetailsEvents.UnblockUser(needsConfirmation = true)) },
            )
        } else {
            PreferenceText(
                title = stringResource(R.string.screen_dm_details_block_user),
                icon = Icons.Outlined.Block,
                tintColor = MaterialTheme.colorScheme.error,
                onClick = { state.eventSink(RoomMemberDetailsEvents.BlockUser(needsConfirmation = true)) },
            )
        }
    }
}

@Composable
internal fun BlockUserDialogs(state: RoomMemberDetailsState) {
    when (state.displayConfirmationDialog) {
        null -> Unit
        RoomMemberDetailsState.ConfirmationDialog.Block -> {
            BlockConfirmationDialog(
                onBlockAction = { state.eventSink(RoomMemberDetailsEvents.BlockUser(needsConfirmation = false)) },
                onDismiss = { state.eventSink(RoomMemberDetailsEvents.ClearConfirmationDialog) }
            )
        }
        RoomMemberDetailsState.ConfirmationDialog.Unblock -> {
            UnblockConfirmationDialog(
                onUnblockAction = { state.eventSink(RoomMemberDetailsEvents.UnblockUser(needsConfirmation = false)) },
                onDismiss = { state.eventSink(RoomMemberDetailsEvents.ClearConfirmationDialog) }
            )
        }
    }
}

@Composable
internal fun BlockConfirmationDialog(onBlockAction: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        content = stringResource(R.string.screen_dm_details_block_alert_description),
        submitText = stringResource(R.string.screen_dm_details_block_alert_action),
        onSubmitClicked = onBlockAction,
        onDismiss = onDismiss
    )
}

@Composable
internal fun UnblockConfirmationDialog(onUnblockAction: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        content = stringResource(R.string.screen_dm_details_unblock_alert_description),
        submitText = stringResource(R.string.screen_dm_details_unblock_alert_action),
        onSubmitClicked = onUnblockAction,
        onDismiss = onDismiss
    )
}
