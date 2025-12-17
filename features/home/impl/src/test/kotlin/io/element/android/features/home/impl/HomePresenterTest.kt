/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.home.impl.roomlist.aRoomListState
import io.element.android.features.home.impl.spaces.HomeSpacesState
import io.element.android.features.home.impl.spaces.aHomeSpacesState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.features.rageshake.test.logs.FakeAnnouncementService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.indicator.test.FakeIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.MutablePresenter
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val isSpaceEnabled = FeatureFlags.Space.defaultValue(aBuildMeta())

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.success(MatrixUser(matrixClient.sessionId, A_USER_NAME, AN_AVATAR_URL)))
        val presenter = createHomePresenter(
            client = matrixClient,
            rageshakeFeatureAvailability = { flowOf(false) },
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(
                        sessionId = matrixClient.sessionId.value,
                        userDisplayName = null,
                        userAvatarUrl = null,
                    )
                ),
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            if (isSpaceEnabled) skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.currentUserAndNeighbors.first()).isEqualTo(
                MatrixUser(A_USER_ID, null, null)
            )
            assertThat(initialState.canReportBug).isFalse()
            skipItems(1)
            val withUserState = awaitItem()
            assertThat(withUserState.currentUserAndNeighbors.first()).isEqualTo(
                MatrixUser(A_USER_ID, A_USER_NAME, AN_AVATAR_URL)
            )
            assertThat(withUserState.showAvatarIndicator).isFalse()
            assertThat(withUserState.isSpaceFeatureEnabled).isEqualTo(isSpaceEnabled)
            assertThat(withUserState.showNavigationBar).isEqualTo(isSpaceEnabled)
        }
    }

    @Test
    fun `present - can report bug`() = runTest {
        val presenter = createHomePresenter(
            rageshakeFeatureAvailability = { flowOf(true) },
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.canReportBug).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.canReportBug).isTrue()
        }
    }

    @Test
    fun `present - space feature enabled`() = runTest {
        val presenter = createHomePresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Space.key to true),
            ),
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isSpaceFeatureEnabled).isTrue()
        }
    }

    @Test
    fun `present - show avatar indicator`() = runTest {
        val indicatorService = FakeIndicatorService()
        val presenter = createHomePresenter(
            indicatorService = indicatorService,
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            if (isSpaceEnabled) skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.showAvatarIndicator).isFalse()
            indicatorService.setShowRoomListTopBarIndicator(true)
            val finalState = awaitItem()
            assertThat(finalState.showAvatarIndicator).isTrue()
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.failure(AN_EXCEPTION))
        val presenter = createHomePresenter(
            client = matrixClient,
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            if (isSpaceEnabled) skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.currentUserAndNeighbors.first()).isEqualTo(MatrixUser(matrixClient.sessionId))
            // No new state is coming
        }
    }

    @Test
    fun `present - NavigationBar change`() = runTest {
        val showAnnouncementResult = lambdaRecorder<Announcement, Unit> { }
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
            announcementService = FakeAnnouncementService(
                showAnnouncementResult = showAnnouncementResult,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            if (isSpaceEnabled) skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            initialState.eventSink(HomeEvents.SelectHomeNavigationBarItem(HomeNavigationBarItem.Spaces))
            val finalState = awaitItem()
            assertThat(finalState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Spaces)
            showAnnouncementResult.assertions().isCalledOnce()
                .with(value(Announcement.Space))
        }
    }

    @Test
    fun `present - NavigationBar is hidden when the last space is left`() = runTest {
        val homeSpacesPresenter = MutablePresenter(aHomeSpacesState())
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Space.key to true),
            ),
            homeSpacesPresenter = homeSpacesPresenter,
            announcementService = FakeAnnouncementService(
                showAnnouncementResult = {},
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            assertThat(initialState.showNavigationBar).isTrue()
            // User navigate to Spaces
            initialState.eventSink(HomeEvents.SelectHomeNavigationBarItem(HomeNavigationBarItem.Spaces))
            val spaceState = awaitItem()
            assertThat(spaceState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Spaces)
            // The last space is left
            homeSpacesPresenter.updateState(aHomeSpacesState(spaceRooms = emptyList()))
            skipItems(1)
            val finalState = awaitItem()
            // We are back to Chats
            assertThat(finalState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            assertThat(finalState.showNavigationBar).isFalse()
        }
    }
}

internal fun createHomePresenter(
    client: MatrixClient = FakeMatrixClient(),
    syncService: SyncService = FakeSyncService(),
    snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(false) },
    indicatorService: IndicatorService = FakeIndicatorService(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
    homeSpacesPresenter: Presenter<HomeSpacesState> = Presenter { aHomeSpacesState() },
    sessionStore: SessionStore = InMemorySessionStore(),
    announcementService: AnnouncementService = FakeAnnouncementService(),
) = HomePresenter(
    client = client,
    syncService = syncService,
    snackbarDispatcher = snackbarDispatcher,
    indicatorService = indicatorService,
    logoutPresenter = { aDirectLogoutState() },
    roomListPresenter = { aRoomListState() },
    homeSpacesPresenter = homeSpacesPresenter,
    rageshakeFeatureAvailability = rageshakeFeatureAvailability,
    featureFlagService = featureFlagService,
    sessionStore = sessionStore,
    announcementService = announcementService,
)
