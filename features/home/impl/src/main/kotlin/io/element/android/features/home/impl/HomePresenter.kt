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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
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
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
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
) : Presenter<HomeState> {
    @Composable
    override fun present(): HomeState {
        val coroutineState = rememberCoroutineScope()
        val matrixUser by client.userProfile.collectAsState()
        val matrixUserAndNeighbors by remember {
            sessionStore.sessionsFlow().map { list ->
                list.takeCurrentUserWithNeighbors(matrixUser).toPersistentList()
            }
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
        LaunchedEffect(matrixUser) {
            // Ensure that the profile is always up to date in our
            // session storage when it changes
            sessionStore.updateUserProfile(
                sessionId = matrixUser.userId.value,
                displayName = matrixUser.displayName,
                avatarUrl = matrixUser.avatarUrl,
            )
        }
        // Avatar indicator
        val showAvatarIndicator by indicatorService.showRoomListTopBarIndicator()
        val directLogoutState = logoutPresenter.present()

        fun handleEvents(event: HomeEvents) {
            when (event) {
                is HomeEvents.SelectHomeNavigationBarItem -> {
                    currentHomeNavigationBarItemOrdinal = event.item.ordinal
                }
                is HomeEvents.SwitchToAccount -> coroutineState.launch {
                    sessionStore.setLatestSession(event.sessionId.value)
                }
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return HomeState(
            matrixUserAndNeighbors = matrixUserAndNeighbors,
            showAvatarIndicator = showAvatarIndicator,
            hasNetworkConnection = isOnline,
            currentHomeNavigationBarItem = currentHomeNavigationBarItem,
            roomListState = roomListState,
            homeSpacesState = homeSpacesState,
            snackbarMessage = snackbarMessage,
            canReportBug = canReportBug,
            directLogoutState = directLogoutState,
            isSpaceFeatureEnabled = isSpaceFeatureEnabled,
            eventSink = ::handleEvents,
        )
    }
}

private fun List<SessionData>.takeCurrentUserWithNeighbors(matrixUser: MatrixUser): List<MatrixUser> {
    // Sort by position to always have the same order (not depending on last account usage)
    return sortedBy { it.position }
        .map {
            if (it.userId == matrixUser.userId.value) {
                // Always use the freshest profile for the current user
                matrixUser
            } else {
                // Use the data from the DB
                MatrixUser(
                    userId = UserId(it.userId),
                    displayName = it.userDisplayName,
                    avatarUrl = it.userAvatarUrl,
                )
            }
        }
        .let { sessionList ->
            // If the list has one item, there is no other session, return the list
            when (sessionList.size) {
                // Can happen when the user signs out (?)
                0 -> listOf(matrixUser)
                1 -> sessionList
                else -> {
                    // Create a list with extra item at the start and end if necessary to have the current user in the middle
                    // If the list is [A, B, C, D] and the current user is A we want to return [D, A, B]
                    // If the current user is B, we want to return [A, B, C]
                    // If the current user is C, we want to return [B, C, D]
                    // If the current user is D, we want to return [C, D, A]
                    val currentUserIndex = sessionList.indexOfFirst { it.userId == matrixUser.userId }
                    when (currentUserIndex) {
                        // This can happen when the user signs out.
                        // In this case, just return a singleton list with the current user.
                        -1 -> listOf(matrixUser)
                        0 -> listOf(sessionList.last()) + sessionList.take(2)
                        sessionList.lastIndex -> sessionList.takeLast(2) + sessionList.first()
                        else -> sessionList.slice(currentUserIndex - 1..currentUserIndex + 1)
                    }
                }
            }
        }
}
