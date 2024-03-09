/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.InvitesEntryPointView
import io.element.android.features.roomlist.impl.InvitesState
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.RoomListContentState
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.SecurityBannerState
import io.element.android.features.roomlist.impl.contentType
import io.element.android.features.roomlist.impl.migration.MigrationScreenView
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomListContentView(
    contentState: RoomListContentState,
    eventSink: (RoomListEvents) -> Unit,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onRoomClicked: (RoomListRoomSummary) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (contentState) {
            is RoomListContentState.Migration -> {
                MigrationScreenView(isMigrating = true)
            }
            is RoomListContentState.Skeleton -> {
                SkeletonView(
                    count = contentState.count,
                )
            }
            is RoomListContentState.Empty -> {
                EmptyView(
                    state = contentState,
                    onInvitesClicked = onInvitesClicked,
                    onCreateRoomClicked = onCreateRoomClicked,
                )
            }
            is RoomListContentState.Rooms -> {
                RoomsView(
                    state = contentState,
                    eventSink = eventSink,
                    onVerifyClicked = onVerifyClicked,
                    onConfirmRecoveryKeyClicked = onConfirmRecoveryKeyClicked,
                    onRoomClicked = onRoomClicked,
                    onRoomLongClicked = onRoomLongClicked,
                    onInvitesClicked = onInvitesClicked,
                )
            }
        }
    }
}

@Composable
private fun SkeletonView(count: Int, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        repeat(count) { index ->
            item {
                RoomSummaryPlaceholderRow()
                if (index != count - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun EmptyView(
    state: RoomListContentState.Empty,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (state.invitesState != InvitesState.NoInvites) {
            InvitesEntryPointView(onInvitesClicked, state.invitesState)
        }
        EmptyScaffold(
            title = {
                Text(
                    text = stringResource(R.string.screen_roomlist_empty_title),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            },
            subtitle = {
                Text(
                    text = stringResource(R.string.screen_roomlist_empty_message),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            },
            action = {
                Button(
                    text = stringResource(CommonStrings.action_start_chat),
                    leadingIcon = IconSource.Vector(CompoundIcons.Compose()),
                    onClick = onCreateRoomClicked,
                )
            },
            modifier = Modifier,
        )
    }
}

@Composable
private fun RoomsView(
    state: RoomListContentState.Rooms,
    eventSink: (RoomListEvents) -> Unit,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onRoomClicked: (RoomListRoomSummary) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
    onInvitesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val visibleRange by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val firstItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val size = layoutInfo.visibleItemsInfo.size
            firstItemIndex until firstItemIndex + size
        }
    }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                eventSink(RoomListEvents.UpdateVisibleRange(visibleRange))
                return super.onPostFling(consumed, available)
            }
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier.nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (state.securityBannerState) {
            SecurityBannerState.SessionVerification -> {
                item {
                    RequestVerificationHeader(
                        onVerifyClicked = onVerifyClicked,
                        onDismissClicked = { eventSink(RoomListEvents.DismissRequestVerificationPrompt) }
                    )
                }
            }
            SecurityBannerState.RecoveryKeyConfirmation -> {
                item {
                    ConfirmRecoveryKeyBanner(
                        onContinueClicked = onConfirmRecoveryKeyClicked,
                        onDismissClicked = { eventSink(RoomListEvents.DismissRecoveryKeyPrompt) }
                    )
                }
            }
            else -> Unit
        }

        if (state.invitesState != InvitesState.NoInvites) {
            item {
                InvitesEntryPointView(onInvitesClicked, state.invitesState)
            }
        }
        // Note: do not use a key for the LazyColumn, or the scroll will not behave as expected if a room
        // is moved to the top of the list.
        itemsIndexed(
            items = state.summaries,
            contentType = { _, room -> room.contentType() },
        ) { index, room ->
            RoomSummaryRow(
                room = room,
                onClick = onRoomClicked,
                onLongClick = onRoomLongClicked,
            )
            if (index != state.summaries.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun EmptyScaffold(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        title()
        Spacer(modifier = Modifier.height(4.dp))
        subtitle?.invoke()
        Spacer(modifier = Modifier.height(16.dp))
        action?.invoke()
    }
}

