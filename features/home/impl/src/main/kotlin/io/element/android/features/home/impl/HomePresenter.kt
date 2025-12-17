/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.home.impl.roomlist.RoomListState
import io.element.android.features.home.impl.spaces.HomeSpacesState
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@Inject
class HomePresenter(
    private val client: MatrixClient,
    private val syncService: SyncService,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val indicatorService: IndicatorService,
    private val roomListPresenter: Presenter<RoomListState>,
    private val homeSpacesPresenter: Presenter<HomeSpacesState>,
    private val logoutPresenter: Presenter<DirectLogoutState>,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val featureFlagService: FeatureFlagService,
    private val sessionStore: SessionStore,
    private val announcementService: AnnouncementService,
) : Presenter<HomeState> {
    private val currentUserWithNeighborsBuilder = CurrentUserWithNeighborsBuilder()

    @Composable
    override fun present(): HomeState {
        val coroutineState = rememberCoroutineScope()
        val matrixUser by client.userProfile.collectAsState()
        val currentUserAndNeighbors by remember {
            combine(
                client.userProfile,
                sessionStore.sessionsFlow(),
                currentUserWithNeighborsBuilder::build,
            )
        }.collectAsState(initial = persistentListOf(matrixUser))
        val isOnline by syncService.isOnline.collectAsState()
        val canReportBug by remember { rageshakeFeatureAvailability.isAvailable() }.collectAsState(false)
        val roomListState = roomListPresenter.present()
        val homeSpacesState = homeSpacesPresenter.present()
        val isSpaceFeatureEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.Space)
        }.collectAsState(initial = false)
        var currentHomeNavigationBarItemOrdinal by rememberSaveable { mutableIntStateOf(HomeNavigationBarItem.Chats.ordinal) }
        val currentHomeNavigationBarItem by remember {
            derivedStateOf {
                HomeNavigationBarItem.from(currentHomeNavigationBarItemOrdinal)
            }
        }
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            client.getUserProfile()
        }
        // Avatar indicator
        val showAvatarIndicator by indicatorService.showRoomListTopBarIndicator()
        val directLogoutState = logoutPresenter.present()

        fun handleEvent(event: HomeEvents) {
            when (event) {
                is HomeEvents.SelectHomeNavigationBarItem -> coroutineState.launch {
                    if (event.item == HomeNavigationBarItem.Spaces) {
                        announcementService.showAnnouncement(Announcement.Space)
                    }
                    currentHomeNavigationBarItemOrdinal = event.item.ordinal
                }
                is HomeEvents.SwitchToAccount -> coroutineState.launch {
                    sessionStore.setLatestSession(event.sessionId.value)
                }
            }
        }

        LaunchedEffect(homeSpacesState.spaceRooms.isEmpty()) {
            // If the last space is left, ensure that the Chat view is rendered.
            if (homeSpacesState.spaceRooms.isEmpty()) {
                currentHomeNavigationBarItemOrdinal = HomeNavigationBarItem.Chats.ordinal
            }
        }
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return HomeState(
            currentUserAndNeighbors = currentUserAndNeighbors,
            showAvatarIndicator = showAvatarIndicator,
            hasNetworkConnection = isOnline,
            currentHomeNavigationBarItem = currentHomeNavigationBarItem,
            roomListState = roomListState,
            homeSpacesState = homeSpacesState,
            snackbarMessage = snackbarMessage,
            canReportBug = canReportBug,
            directLogoutState = directLogoutState,
            isSpaceFeatureEnabled = isSpaceFeatureEnabled,
            eventSink = ::handleEvent,
        )
    }
}
