/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.home.impl.roomlist.RoomListState
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
import javax.inject.Inject

class HomePresenter @Inject constructor(
    private val client: MatrixClient,
    private val syncService: SyncService,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val indicatorService: IndicatorService,
    private val roomListPresenter: Presenter<RoomListState>,
    private val logoutPresenter: Presenter<DirectLogoutState>,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val featureFlagService: FeatureFlagService,
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {
        val matrixUser = client.userProfile.collectAsState()
        val isOnline by syncService.isOnline.collectAsState()
        val canReportBug = remember { rageshakeFeatureAvailability.isAvailable() }
        val roomListState = roomListPresenter.present()
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

        fun handleEvents(event: HomeEvents) {
            when (event) {
                is HomeEvents.SelectHomeNavigationBarItem -> {
                    currentHomeNavigationBarItemOrdinal = event.item.ordinal
                }
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return HomeState(
            matrixUser = matrixUser.value,
            showAvatarIndicator = showAvatarIndicator,
            hasNetworkConnection = isOnline,
            currentHomeNavigationBarItem = currentHomeNavigationBarItem,
            roomListState = roomListState,
            snackbarMessage = snackbarMessage,
            canReportBug = canReportBug,
            directLogoutState = directLogoutState,
            isSpaceFeatureEnabled = isSpaceFeatureEnabled,
            eventSink = ::handleEvents,
        )
    }
}
