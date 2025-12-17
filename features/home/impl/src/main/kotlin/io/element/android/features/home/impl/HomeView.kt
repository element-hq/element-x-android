/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalHazeMaterialsApi::class)

package io.element.android.features.home.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.components.HomeTopBar
import io.element.android.features.home.impl.components.RoomListContentView
import io.element.android.features.home.impl.components.RoomListMenuAction
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.RoomListContextMenu
import io.element.android.features.home.impl.roomlist.RoomListDeclineInviteMenu
import io.element.android.features.home.impl.roomlist.RoomListEvents
import io.element.android.features.home.impl.roomlist.RoomListState
import io.element.android.features.home.impl.search.RoomListSearchView
import io.element.android.features.home.impl.spaces.HomeSpacesView
import io.element.android.libraries.androidutils.throttler.FirstThrottler
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.NavigationBar
import io.element.android.libraries.designsystem.theme.components.NavigationBarIcon
import io.element.android.libraries.designsystem.theme.components.NavigationBarItem
import io.element.android.libraries.designsystem.theme.components.NavigationBarText
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.launch

@Composable
fun HomeView(
    homeState: HomeState,
    onRoomClick: (RoomId) -> Unit,
    onSettingsClick: () -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onStartChatClick: () -> Unit,
    onRoomSettingsClick: (roomId: RoomId) -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onReportRoomClick: (roomId: RoomId) -> Unit,
    onDeclineInviteAndBlockUser: (roomSummary: RoomListRoomSummary) -> Unit,
    acceptDeclineInviteView: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leaveRoomView: @Composable () -> Unit,
) {
    val state: RoomListState = homeState.roomListState
    val coroutineScope = rememberCoroutineScope()
    val firstThrottler = remember { FirstThrottler(300, coroutineScope) }
    Box(modifier) {
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

        leaveRoomView()

        HomeScaffold(
            state = homeState,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onRoomClick = { if (firstThrottler.canHandle()) onRoomClick(it) },
            onOpenSettings = { if (firstThrottler.canHandle()) onSettingsClick() },
            onStartChatClick = { if (firstThrottler.canHandle()) onStartChatClick() },
            onMenuActionClick = onMenuActionClick,
        )
        // This overlaid view will only be visible when state.displaySearchResults is true
        RoomListSearchView(
            state = state.searchState,
            eventSink = state.eventSink,
            hideInvitesAvatars = state.hideInvitesAvatars,
            onRoomClick = { if (firstThrottler.canHandle()) onRoomClick(it) },
            modifier = Modifier
                .fillMaxSize()
                .background(ElementTheme.colors.bgCanvasDefault)
        )
        acceptDeclineInviteView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    state: HomeState,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onOpenSettings: () -> Unit,
    onStartChatClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    val roomListState: RoomListState = state.roomListState

    BackHandler(
        enabled = state.currentHomeNavigationBarItem != HomeNavigationBarItem.Chats,
    ) {
        state.eventSink(HomeEvents.SelectHomeNavigationBarItem(HomeNavigationBarItem.Chats))
    }

    val hazeState = rememberHazeState()
    val roomsLazyListState = rememberLazyListState()
    val spacesLazyListState = rememberLazyListState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopBar(
                title = stringResource(state.currentHomeNavigationBarItem.labelRes),
                currentUserAndNeighbors = state.currentUserAndNeighbors,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = roomListState.searchState.isSearchActive,
                onToggleSearch = { roomListState.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClick = onMenuActionClick,
                onOpenSettings = onOpenSettings,
                onAccountSwitch = {
                    state.eventSink(HomeEvents.SwitchToAccount(it))
                },
                scrollBehavior = scrollBehavior,
                displayMenuItems = state.displayActions,
                displayFilters = state.displayRoomListFilters,
                filtersState = roomListState.filtersState,
                canReportBug = state.canReportBug,
                modifier = if (state.isSpaceFeatureEnabled) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thick(),
                    )
                } else {
                    Modifier.background(ElementTheme.colors.bgCanvasDefault)
                }
            )
        },
        bottomBar = {
            if (state.showNavigationBar) {
                val coroutineScope = rememberCoroutineScope()
                HomeBottomBar(
                    currentHomeNavigationBarItem = state.currentHomeNavigationBarItem,
                    onItemClick = { item ->
                        // scroll to top if selecting the same item
                        if (item == state.currentHomeNavigationBarItem) {
                            val lazyListStateTarget = when (item) {
                                HomeNavigationBarItem.Chats -> roomsLazyListState
                                HomeNavigationBarItem.Spaces -> spacesLazyListState
                            }
                            coroutineScope.launch {
                                if (lazyListStateTarget.firstVisibleItemIndex > 10) {
                                    lazyListStateTarget.scrollToItem(10)
                                }
                                // Also reset the scrollBehavior height offset as it's not triggered by programmatic scrolls
                                scrollBehavior.state.heightOffset = 0f
                                lazyListStateTarget.animateScrollToItem(0)
                            }
                        } else {
                            state.eventSink(HomeEvents.SelectHomeNavigationBarItem(item))
                        }
                    },
                    modifier = Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thick(),
                    )
                )
            }
        },
        content = { padding ->
            when (state.currentHomeNavigationBarItem) {
                HomeNavigationBarItem.Chats -> {
                    RoomListContentView(
                        contentState = roomListState.contentState,
                        filtersState = roomListState.filtersState,
                        lazyListState = roomsLazyListState,
                        hideInvitesAvatars = roomListState.hideInvitesAvatars,
                        eventSink = roomListState.eventSink,
                        onSetUpRecoveryClick = onSetUpRecoveryClick,
                        onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                        onRoomClick = ::onRoomClick,
                        onCreateRoomClick = onStartChatClick,
                        contentPadding = PaddingValues(
                            // FAB height is 56dp, bottom padding is 16dp, we add 8dp as extra margin -> 56+16+8 = 80,
                            // and include provided bottom padding
                            // Disable contentPadding due to navigation issue using the keyboard
                            // See https://issuetracker.google.com/issues/436432313
                            bottom = 80.dp,
                            // bottom = 80.dp + padding.calculateBottomPadding(),
                            // top = padding.calculateTopPadding()
                        ),
                        modifier = Modifier
                            .padding(
                                PaddingValues(
                                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                                    // Remove these two lines once https://issuetracker.google.com/issues/436432313 has been fixed
                                    bottom = padding.calculateBottomPadding(),
                                    top = padding.calculateTopPadding()
                                )
                            )
                            .consumeWindowInsets(padding)
                            .hazeSource(state = hazeState)
                    )
                }
                HomeNavigationBarItem.Spaces -> {
                    HomeSpacesView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .hazeSource(state = hazeState),
                        state = state.homeSpacesState,
                        lazyListState = spacesLazyListState,
                        onSpaceClick = { spaceId ->
                            onRoomClick(spaceId)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.displayActions) {
                FloatingActionButton(
                    onClick = onStartChatClick,
                ) {
                    Icon(
                        imageVector = CompoundIcons.Plus(),
                        contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@Composable
private fun HomeBottomBar(
    currentHomeNavigationBarItem: HomeNavigationBarItem,
    onItemClick: (HomeNavigationBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = Color.Transparent,
        modifier = modifier
    ) {
        HomeNavigationBarItem.entries.forEach { item ->
            val isSelected = currentHomeNavigationBarItem == item
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    onItemClick(item)
                },
                icon = {
                    NavigationBarIcon(
                        imageVector = item.icon(isSelected),
                    )
                },
                label = {
                    NavigationBarText(
                        text = stringResource(item.labelRes),
                    )
                }
            )
        }
    }
}

internal fun RoomListRoomSummary.contentType() = displayType.ordinal

@PreviewsDayNight
@Composable
internal fun HomeViewPreview(@PreviewParameter(HomeStateProvider::class) state: HomeState) = ElementPreview {
    HomeView(
        homeState = state,
        onRoomClick = {},
        onSettingsClick = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onStartChatClick = {},
        onRoomSettingsClick = {},
        onReportRoomClick = {},
        onMenuActionClick = {},
        onDeclineInviteAndBlockUser = {},
        acceptDeclineInviteView = {},
        leaveRoomView = {}
    )
}

@Preview
@Composable
internal fun HomeViewA11yPreview() = ElementPreview {
    HomeView(
        homeState = aHomeState(),
        onRoomClick = {},
        onSettingsClick = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onStartChatClick = {},
        onRoomSettingsClick = {},
        onReportRoomClick = {},
        onMenuActionClick = {},
        onDeclineInviteAndBlockUser = {},
        acceptDeclineInviteView = {},
        leaveRoomView = {}
    )
}
