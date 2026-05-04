/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.contentType
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.features.home.impl.filters.RoomListFiltersEmptyStateResources
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.filters.aRoomListFiltersState
import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.model.RoomSummaryDisplayType
import io.element.android.features.home.impl.roomlist.RoomListContentState
import io.element.android.features.home.impl.roomlist.RoomListContentStateProvider
import io.element.android.features.home.impl.roomlist.RoomListEvent
import io.element.android.features.home.impl.roomlist.SecurityBannerState
import io.element.android.features.home.impl.spacefilters.SpaceFiltersState
import io.element.android.features.home.impl.spacefilters.anUnselectedSpaceFiltersState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun RoomListContentView(
    contentState: RoomListContentState,
    filtersState: RoomListFiltersState,
    spaceFiltersState: SpaceFiltersState,
    lazyListState: LazyListState,
    hideInvitesAvatars: Boolean,
    eventSink: (RoomListEvent) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    onCreateRoomClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    when (contentState) {
        is RoomListContentState.Skeleton -> {
            SkeletonView(
                modifier = modifier,
                count = contentState.count,
                contentPadding = contentPadding,
            )
        }
        is RoomListContentState.Empty -> {
            EmptyView(
                modifier = modifier.padding(contentPadding),
                state = contentState,
                eventSink = eventSink,
                onSetUpRecoveryClick = onSetUpRecoveryClick,
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onCreateRoomClick = onCreateRoomClick,
            )
        }
        is RoomListContentState.Rooms -> {
            RoomsView(
                modifier = modifier,
                state = contentState,
                hideInvitesAvatars = hideInvitesAvatars,
                filtersState = filtersState,
                spaceFiltersState = spaceFiltersState,
                eventSink = eventSink,
                onSetUpRecoveryClick = onSetUpRecoveryClick,
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onRoomClick = onRoomClick,
                lazyListState = lazyListState,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun SkeletonView(
    count: Int,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
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
    eventSink: (RoomListEvent) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        EmptyScaffold(
            title = R.string.screen_roomlist_empty_title,
            subtitle = R.string.screen_roomlist_empty_message,
            action = {
                Button(
                    text = stringResource(CommonStrings.action_start_chat),
                    leadingIcon = IconSource.Vector(CompoundIcons.Compose()),
                    onClick = onCreateRoomClick,
                )
            },
            modifier = Modifier.align(Alignment.Center),
        )
        Box {
            when (state.securityBannerState) {
                SecurityBannerState.SetUpRecovery -> {
                    SetUpRecoveryKeyBanner(
                        onContinueClick = onSetUpRecoveryClick,
                        onDismissClick = { eventSink(RoomListEvent.DismissBanner) },
                    )
                }
                SecurityBannerState.RecoveryKeyConfirmation -> {
                    ConfirmRecoveryKeyBanner(
                        onContinueClick = onConfirmRecoveryKeyClick,
                        onDismissClick = { eventSink(RoomListEvent.DismissBanner) },
                    )
                }
                SecurityBannerState.None -> Unit
            }
        }
    }
}

@Composable
private fun RoomsView(
    state: RoomListContentState.Rooms,
    hideInvitesAvatars: Boolean,
    filtersState: RoomListFiltersState,
    spaceFiltersState: SpaceFiltersState,
    eventSink: (RoomListEvent) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    contentPadding: PaddingValues,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val isSpaceFilterSelected = spaceFiltersState is SpaceFiltersState.Selected
    val hasAnyFilterSelected = filtersState.hasAnyFilterSelected || isSpaceFilterSelected
    if (state.summaries.isEmpty() && hasAnyFilterSelected) {
        EmptyViewForFilterStates(
            selectedFilters = filtersState.selectedFilters(),
            isSpaceFilterSelected = isSpaceFilterSelected,
            modifier = modifier.fillMaxSize()
        )
    } else {
        RoomsViewList(
            state = state,
            hideInvitesAvatars = hideInvitesAvatars,
            spaceFiltersState = spaceFiltersState,
            eventSink = eventSink,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onRoomClick = onRoomClick,
            contentPadding = contentPadding,
            lazyListState = lazyListState,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RoomsViewList(
    state: RoomListContentState.Rooms,
    hideInvitesAvatars: Boolean,
    spaceFiltersState: SpaceFiltersState,
    eventSink: (RoomListEvent) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    contentPadding: PaddingValues,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val roomToSpaceAvatarMap = remember(spaceFiltersState.availableFilters) {
        if (spaceFiltersState is SpaceFiltersState.Disabled) {
            emptyMap<RoomId, AvatarData>()
        } else {
            buildRoomToSpaceAvatarMap(spaceFiltersState.availableFilters)
        }
    }
    OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
        eventSink(RoomListEvent.UpdateVisibleRange(visibleRange))
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        when (state.securityBannerState) {
            SecurityBannerState.SetUpRecovery -> {
                item {
                    SetUpRecoveryKeyBanner(
                        onContinueClick = onSetUpRecoveryClick,
                        onDismissClick = { eventSink(RoomListEvent.DismissBanner) },
                    )
                }
            }
            SecurityBannerState.RecoveryKeyConfirmation -> {
                item {
                    ConfirmRecoveryKeyBanner(
                        onContinueClick = onConfirmRecoveryKeyClick,
                        onDismissClick = { eventSink(RoomListEvent.DismissBanner) },
                    )
                }
            }
            SecurityBannerState.None -> if (state.fullScreenIntentPermissionsState.shouldDisplayBanner) {
                item {
                    FullScreenIntentPermissionBanner(state = state.fullScreenIntentPermissionsState)
                }
            } else if (state.batteryOptimizationState.shouldDisplayBanner) {
                item {
                    BatteryOptimizationBanner(state = state.batteryOptimizationState)
                }
            } else if (state.showNewNotificationSoundBanner) {
                item {
                    NewNotificationSoundBanner(
                        onDismissClick = { eventSink(RoomListEvent.DismissNewNotificationSoundBanner) },
                    )
                }
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
                hideInviteAvatars = hideInvitesAvatars,
                isInviteSeen = room.displayType == RoomSummaryDisplayType.INVITE &&
                    state.seenRoomInvites.contains(room.roomId),
                spaceAvatarData = roomToSpaceAvatarMap[room.roomId],
                onClick = onRoomClick,
                eventSink = eventSink,
            )
            if (index != state.summaries.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun EmptyViewForFilterStates(
    selectedFilters: ImmutableList<RoomListFilter>,
    isSpaceFilterSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val emptyStateResources = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters, isSpaceFilterSelected) ?: return
    EmptyScaffold(
        title = emptyStateResources.title,
        subtitle = emptyStateResources.subtitle,
        modifier = modifier,
    )
}

@Composable
private fun EmptyScaffold(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    modifier: Modifier = Modifier,
    action: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(title),
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(subtitle),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        action?.invoke(this)
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListContentViewPreview(@PreviewParameter(RoomListContentStateProvider::class) state: RoomListContentState) = ElementPreview {
    RoomListContentView(
        contentState = state,
        filtersState = aRoomListFiltersState(
            filterSelectionStates = RoomListFilter.entries.map {
                FilterSelectionState(
                    filter = it,
                    isSelected = true
                )
            }
        ),
        spaceFiltersState = anUnselectedSpaceFiltersState(),
        hideInvitesAvatars = false,
        eventSink = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onRoomClick = {},
        onCreateRoomClick = {},
        lazyListState = rememberLazyListState(),
        contentPadding = PaddingValues(0.dp),
    )
}

private fun buildRoomToSpaceAvatarMap(
    availableFilters: ImmutableList<SpaceServiceFilter>,
): ImmutableMap<RoomId, AvatarData> {
    val map = mutableMapOf<RoomId, AvatarData>()
    for (filter in availableFilters) {
        val spaceAvatarData = AvatarData(
            id = filter.spaceRoom.roomId.value,
            name = filter.spaceRoom.displayName,
            url = filter.spaceRoom.avatarUrl,
            size = AvatarSize.SpaceAvatarInRoomList,
        )
        for (roomId in filter.descendants) {
            if (roomId !in map) {
                map[roomId] = spaceAvatarData
            }
        }
    }
    return map.toImmutableMap()
}
