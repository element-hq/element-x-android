/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarRow
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import io.element.android.libraries.designsystem.utils.lazyColumnContentPadding
import io.element.android.libraries.designsystem.utils.scaffoldScrollableContentInsets
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserHeader
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.matrix.ui.components.SelectedRoom
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=195-38510
 */
@Suppress("MultipleEmitters") // False positive
@Composable
fun RoomSelectView(
    state: RoomSelectState,
    onDismiss: () -> Unit,
    onSubmit: (List<RoomId>) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomRemoved(roomInfo: SelectRoomInfo) {
        state.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomInfo))
    }

    @Composable
    fun SelectedRoomsHelper(
        selectedRooms: ImmutableList<SelectRoomInfo>,
        showVerticalSpace: Boolean,
    ) {
        if (selectedRooms.isNotEmpty()) {
            SelectedRooms(
                selectedRooms = selectedRooms,
                onRemoveRoom = ::onRoomRemoved,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else if (showVerticalSpace) {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    var canHandleBack by remember { mutableStateOf(true) }
    fun onBackButton(state: RoomSelectState) {
        if (state.isSearchActive) {
            state.eventSink(RoomSelectEvent.ToggleSearchActive)
        } else if (canHandleBack) {
            canHandleBack = false
            onDismiss()
        }
    }

    BackHandler(
        enabled = canHandleBack,
        onBack = { onBackButton(state) }
    )

    // Use a new list state when the selected account changes, so that the list of rooms is scrolled to the top
    val lazyListState = key(state.selectedAccount.userId) { rememberLazyListState() }
    OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
        state.eventSink(RoomSelectEvent.UpdateVisibleRange(visibleRange))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = when (state.mode) {
                    RoomSelectMode.Forward -> stringResource(CommonStrings.common_forward_message)
                    RoomSelectMode.Share -> stringResource(CommonStrings.common_send_to)
                },
                navigationIcon = {
                    BackButton(
                        enabled = canHandleBack,
                        onClick = { onBackButton(state) }
                    )
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_send),
                        enabled = state.selectedRooms.isNotEmpty(),
                        onClick = { onSubmit(state.selectedRooms.map { it.roomId }) }
                    )
                }
            )
        },
        contentWindowInsets = scaffoldScrollableContentInsets,
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        WindowInsets.safeDrawing
                            .only(WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    ),
                placeHolderTitle = stringResource(CommonStrings.action_search),
                queryState = state.searchQuery,
                active = state.isSearchActive,
                onActiveChange = { state.eventSink(RoomSelectEvent.ToggleSearchActive) },
                resultState = state.resultState,
                showBackButton = false,
            ) { summaries ->
                if (summaries.isNotEmpty()) {
                    ListSectionHeader(
                        title = stringResource(CommonStrings.common_header_rooms),
                        hasDivider = false,
                    )
                }
                LazyColumn(
                    state = lazyListState,
                    contentPadding = lazyColumnContentPadding,
                ) {
                    item {
                        SelectedRoomsHelper(
                            selectedRooms = state.selectedRooms,
                            showVerticalSpace = false,
                        )
                    }
                    items(summaries, key = { it.roomId.value }) { roomSummary ->
                        RoomSummaryView(
                            roomSummary,
                            isSelected = state.selectedRooms.any { it.roomId == roomSummary.roomId },
                            onSelection = { roomSummary ->
                                state.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomSummary))
                            },
                            canBeSelected = state.canSelectMoreRooms,
                        )
                    }
                }
            }

            if (!state.isSearchActive) {
                if (state.showAccountSwitch) {
                    AccountSwitchSection(
                        selectedAccount = state.selectedAccount,
                        otherAccounts = state.otherAccounts,
                        isExpanded = state.isAccountListExpanded,
                        onToggleExpand = { state.eventSink(RoomSelectEvent.ToggleAccountListExpanded) },
                        onSelectAccount = { state.eventSink(RoomSelectEvent.SelectAccount(it)) },
                    )
                }
                SelectedRoomsHelper(
                    selectedRooms = state.selectedRooms,
                    // showVerticalSpace only if there is no other accounts
                    showVerticalSpace = !state.showAccountSwitch,
                )
                if (state.resultState is SearchBarResultState.Results) {
                    if (state.resultState.results.isNotEmpty()) {
                        ListSectionHeader(
                            title = stringResource(CommonStrings.common_header_rooms),
                            hasDivider = false,
                        )
                    }
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = lazyColumnContentPadding,
                    ) {
                        items(state.resultState.results, key = { it.roomId.value }) { roomSummary ->
                            RoomSummaryView(
                                roomSummary,
                                isSelected = state.selectedRooms.any { it.roomId == roomSummary.roomId },
                                onSelection = { roomSummary ->
                                    state.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomSummary))
                                },
                                canBeSelected = state.canSelectMoreRooms,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Similar to the MultiAccountSection of the PreferencesRootView.
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=5414-4759
 */
@Composable
private fun AccountSwitchSection(
    selectedAccount: MatrixUser,
    otherAccounts: ImmutableList<MatrixUser>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectAccount: (SessionId) -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        MatrixUserHeader(
            matrixUser = selectedAccount,
        )
        HorizontalDivider(
            thickness = 8.dp,
            color = ElementTheme.colors.bgSubtleSecondary,
        )
        val expandedStateDescription = if (isExpanded) {
            stringResource(CommonStrings.a11y_state_expanded)
        } else {
            stringResource(CommonStrings.a11y_state_collapsed)
        }
        ListItem(
            modifier = Modifier.semantics {
                stateDescription = expandedStateDescription
            },
            content = { Text(stringResource(CommonStrings.common_switch_account)) },
            onClick = onToggleExpand,
            trailingContent = ListItemContent.Custom { _ ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AnimatedVisibility(
                        visible = !isExpanded,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        AvatarRow(
                            avatarDataList = otherAccounts
                                .take(3)
                                .map { it.getAvatarData(AvatarSize.OtherAccountItem) }
                                .toImmutableList(),
                            avatarType = AvatarType.User,
                            lastOnTop = true,
                        )
                    }
                    // Animate the chevron icon to rotate when the section is expanded/collapsed
                    val rotation: Float by animateFloatAsState(
                        targetValue = if (isExpanded) -180f else 0f,
                        animationSpec = tween(
                            delayMillis = 0,
                            durationMillis = 300,
                        ),
                        label = "chevron"
                    )
                    Icon(
                        modifier = Modifier.rotate(rotation),
                        imageVector = CompoundIcons.ChevronDown(),
                        contentDescription = null,
                    )
                }
            },
        )
        AnimatedVisibility(
            visible = isExpanded,
        ) {
            Column {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = ElementTheme.colors.bgSubtleSecondary,
                )
                otherAccounts.forEach { matrixUser ->
                    MatrixUserRow(
                        modifier = Modifier
                            .clickable {
                                onSelectAccount(matrixUser.userId)
                            }
                            .padding(top = 2.dp, bottom = 2.dp, end = 8.dp),
                        matrixUser = matrixUser,
                        avatarSize = AvatarSize.AccountItem,
                        verticalSpaceWidth = 16.dp,
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 8.dp,
            color = ElementTheme.colors.bgSubtleSecondary,
        )
    }
}

@Composable
private fun SelectedRooms(
    selectedRooms: ImmutableList<SelectRoomInfo>,
    onRemoveRoom: (SelectRoomInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(selectedRooms, key = { it.roomId.value }) { selectRoomInfo ->
            SelectedRoom(roomInfo = selectRoomInfo, onRemoveRoom = onRemoveRoom)
        }
    }
}

@Composable
private fun RoomSummaryView(
    roomInfo: SelectRoomInfo,
    isSelected: Boolean,
    canBeSelected: Boolean,
    onSelection: (SelectRoomInfo) -> Unit,
) {
    ListItem(
        onClick = { onSelection(roomInfo) },
        leadingContent = ListItemContent.Custom {
            Avatar(
                avatarData = roomInfo.getAvatarData(size = AvatarSize.RoomSelectRoomListItem),
                avatarType = AvatarType.Room(
                    heroes = roomInfo.heroes.map { user ->
                        user.getAvatarData(size = AvatarSize.RoomSelectRoomListItem)
                    }.toImmutableList(),
                    isTombstoned = roomInfo.isTombstoned,
                ),
            )
        },
        trailingContent = ListItemContent.Checkbox(
            checked = isSelected,
            enabled = isSelected || canBeSelected,
        ),
    ) {
        Column {
            // Name
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = roomInfo.name ?: stringResource(id = CommonStrings.common_no_room_name),
                fontStyle = FontStyle.Italic.takeIf { roomInfo.name == null },
                color = ElementTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val otherUserId = roomInfo.heroes.singleOrNull()?.userId?.takeIf { roomInfo.isDm }
            val subtitle = roomInfo.canonicalAlias?.value ?: otherUserId?.value
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomSelectViewPreview(@PreviewParameter(RoomSelectStatePreviewParam::class) state: RoomSelectState) = ElementPreview {
    RoomSelectView(
        state = state,
        onDismiss = {},
        onSubmit = {},
    )
}
