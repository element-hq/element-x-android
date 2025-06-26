/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalHazeMaterialsApi::class)

package io.element.android.features.roomlist.impl

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.roomlist.impl.components.RoomListContentView
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchView
import io.element.android.libraries.androidutils.throttler.FirstThrottler
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.NavigationBar
import io.element.android.libraries.designsystem.theme.components.NavigationBarItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId

@Composable
fun RoomListView(
    state: RoomListState,
    onRoomClick: (RoomId) -> Unit,
    onSettingsClick: () -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onRoomSettingsClick: (roomId: RoomId) -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onReportRoomClick: (roomId: RoomId) -> Unit,
    onDeclineInviteAndBlockUser: (roomSummary: RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
    acceptDeclineInviteView: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val firstThrottler = remember { FirstThrottler(300, coroutineScope) }

    ConnectivityIndicatorContainer(
        modifier = modifier,
        isOnline = state.hasNetworkConnection,
    ) { topPadding ->
        Box {
            if (state.contextMenu is RoomListState.ContextMenu.Shown) {
                RoomListContextMenu(
                    contextMenu = state.contextMenu,
                    canReportRoom = state.canReportRoom,
                    eventSink = state.eventSink,
                    onRoomSettingsClick = onRoomSettingsClick,
                    onReportRoomClick = onReportRoomClick,
                )
            }
            if (state.declineInviteMenu is RoomListState.DeclineInviteMenu.Shown) {
                RoomListDeclineInviteMenu(
                    menu = state.declineInviteMenu,
                    canReportRoom = state.canReportRoom,
                    eventSink = state.eventSink,
                    onDeclineAndBlockClick = onDeclineInviteAndBlockUser,
                )
            }

            LeaveRoomView(state = state.leaveRoomState)

            RoomListScaffold(
                state = state,
                onSetUpRecoveryClick = onSetUpRecoveryClick,
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onRoomClick = { if (firstThrottler.canHandle()) onRoomClick(it) },
                onOpenSettings = { if (firstThrottler.canHandle()) onSettingsClick() },
                onCreateRoomClick = { if (firstThrottler.canHandle()) onCreateRoomClick() },
                onMenuActionClick = onMenuActionClick,
                modifier = Modifier.padding(top = topPadding),
            )
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchView(
                state = state.searchState,
                eventSink = state.eventSink,
                hideInvitesAvatars = state.hideInvitesAvatars,
                onRoomClick = { if (firstThrottler.canHandle()) onRoomClick(it) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = topPadding)
                    .fillMaxSize()
                    .background(ElementTheme.colors.bgCanvasDefault)
            )
            acceptDeclineInviteView()
        }
    }
}

private enum class HomeNavigationBarItem(
    @StringRes
    val labelRes: Int,
) {
    Chats(
        labelRes = R.string.screen_roomlist_main_space_title
    ),
    Spaces(
        // TODO Create a new entry in Localazy
        labelRes = R.string.screen_roomlist_main_space_title
    );

    @Composable
    fun icon() = when (this) {
        Chats -> CompoundIcons.ChatSolid()
        // TODO Spaces -> CompoundIcons.Workspace()
        Spaces -> CompoundIcons.Code()
    }

    companion object {
        fun from(index: Int): HomeNavigationBarItem {
            return entries.getOrElse(index) { Chats }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListScaffold(
    state: RoomListState,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onOpenSettings: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    var currentHomeNavigationBarItemOrdinal by rememberSaveable { mutableIntStateOf(HomeNavigationBarItem.Chats.ordinal) }
    val currentHomeNavigationBarItem by remember {
        derivedStateOf {
            HomeNavigationBarItem.from(currentHomeNavigationBarItemOrdinal)
        }
    }

    BackHandler(
        enabled = currentHomeNavigationBarItem != HomeNavigationBarItem.Chats,
    ) {
        currentHomeNavigationBarItemOrdinal = HomeNavigationBarItem.Chats.ordinal
    }

    val hazeState = rememberHazeState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RoomListTopBar(
                title = stringResource(currentHomeNavigationBarItem.labelRes),
                matrixUser = state.matrixUser,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = state.searchState.isSearchActive,
                onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClick = onMenuActionClick,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
                displayMenuItems = state.displayActions,
                displayFilters = state.displayFilters && currentHomeNavigationBarItem == HomeNavigationBarItem.Chats,
                filtersState = state.filtersState,
                canReportBug = state.canReportBug,
            )
        },
        bottomBar = {
            if (state.isSpaceFeatureEnabled) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.regular(),
                        )
                ) {
                    HomeNavigationBarItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentHomeNavigationBarItem == item,
                            onClick = {
                                currentHomeNavigationBarItemOrdinal = item.ordinal
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon(),
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(stringResource(item.labelRes))
                            }
                        )
                    }
                }
            }
        },
        content = { padding ->
            when (currentHomeNavigationBarItem) {
                HomeNavigationBarItem.Chats -> {
                    RoomListContentView(
                        contentState = state.contentState,
                        filtersState = state.filtersState,
                        hideInvitesAvatars = state.hideInvitesAvatars,
                        eventSink = state.eventSink,
                        onSetUpRecoveryClick = onSetUpRecoveryClick,
                        onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                        onRoomClick = ::onRoomClick,
                        onCreateRoomClick = onCreateRoomClick,
                        // FAB height is 56dp, bottom padding is 16dp, we add 8dp as extra margin -> 56+16+8 = 80,
                        // and include provided bottom padding
                        contentBottomPadding = 80.dp + padding.calculateBottomPadding(),
                        modifier = Modifier
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = 0.dp,
                                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                                end = padding.calculateEndPadding(LocalLayoutDirection.current),
                            )
                            .consumeWindowInsets(padding)
                            .hazeSource(state = hazeState)
                    )
                }
                HomeNavigationBarItem.Spaces -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Spaces are coming soon!",
                            style = ElementTheme.typography.fontBodyLgRegular,
                            color = ElementTheme.colors.textPrimary,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.displayActions && currentHomeNavigationBarItem == HomeNavigationBarItem.Chats) {
                FloatingActionButton(
                    containerColor = ElementTheme.colors.iconPrimary,
                    onClick = onCreateRoomClick
                ) {
                    Icon(
                        imageVector = CompoundIcons.Plus(),
                        contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message),
                        tint = ElementTheme.colors.iconOnSolidPrimary,
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

internal fun RoomListRoomSummary.contentType() = displayType.ordinal

@PreviewsDayNight
@Composable
internal fun RoomListViewPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) = ElementPreview {
    RoomListView(
        state = state,
        onRoomClick = {},
        onSettingsClick = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onCreateRoomClick = {},
        onRoomSettingsClick = {},
        onReportRoomClick = {},
        onMenuActionClick = {},
        onDeclineInviteAndBlockUser = {},
        acceptDeclineInviteView = {},
    )
}
