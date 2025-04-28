/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.MatrixConfiguration
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListDeclineInviteMenu(
    menu: RoomListState.DeclineInviteMenu.Shown,
    onDeclineAndBlockClick: (RoomListRoomSummary) -> Unit,
    eventSink: (RoomListEvents) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { eventSink(RoomListEvents.HideDeclineInviteMenu) },
    ) {
        BottomSheetContent(
            roomName = menu.roomSummary.name ?: menu.roomSummary.roomId.value,
            onDeclineClick = {
                eventSink(RoomListEvents.HideDeclineInviteMenu)
                eventSink(RoomListEvents.DeclineInvite(menu.roomSummary, false))
            },
            onDeclineAndBlockClick = {
                eventSink(RoomListEvents.HideDeclineInviteMenu)
                if (MatrixConfiguration.CAN_REPORT_ROOM) {
                    onDeclineAndBlockClick(menu.roomSummary)
                } else {
                    eventSink(RoomListEvents.DeclineInvite(menu.roomSummary, true))
                }
            },
            onCancelClick = {
                eventSink(RoomListEvents.HideDeclineInviteMenu)
            }
        )
    }
}

@Composable
private fun BottomSheetContent(
    roomName: String,
    onDeclineClick: () -> Unit,
    onDeclineAndBlockClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.screen_invites_decline_chat_title),
            style = ElementTheme.typography.fontHeadingSmMedium,
            color = ElementTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.screen_invites_decline_chat_message, roomName),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(22.dp))
        Button(
            text = stringResource(CommonStrings.action_decline),
            modifier = Modifier.fillMaxWidth(),
            onClick = onDeclineClick,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            text = stringResource(CommonStrings.action_decline_and_block),
            modifier = Modifier.fillMaxWidth(),
            destructive = true,
            onClick = onDeclineAndBlockClick
        )
        Spacer(Modifier.height(16.dp))
        TextButton(
            text = stringResource(CommonStrings.action_cancel),
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancelClick
        )
    }
}
