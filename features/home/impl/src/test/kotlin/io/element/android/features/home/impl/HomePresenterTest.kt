/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.home.impl.roomlist.aRoomListState
import io.element.android.features.home.impl.spaces.HomeSpacesState
import io.element.android.features.home.impl.spaces.aHomeSpacesState
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
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
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
        presenter.test {
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
        }
    }

    @Test
    fun `present - can report bug`() = runTest {
        val presenter = createHomePresenter(
            rageshakeFeatureAvailability = { flowOf(true) },
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.canReportBug).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.canReportBug).isTrue()
        }
    }

    @Test
    fun `present - show avatar indicator`() = runTest {
        val indicatorService = FakeIndicatorService()
        val presenter = createHomePresenter(
            indicatorService = indicatorService,
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _, _ -> },
            ),
        )
        presenter.test {
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
                updateUserProfileResult = { _, _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.currentUserAndNeighbors.first()).isEqualTo(MatrixUser(matrixClient.sessionId))
            // No new state is coming
        }
    }

    @Test
    fun `present - NavigationBar change`() = runTest {
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            initialState.eventSink(HomeEvent.SelectHomeNavigationBarItem(HomeNavigationBarItem.Spaces))
            val finalState = awaitItem()
            assertThat(finalState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Spaces)
        }
    }

    @Test
    fun `present - multi account announcement is shown when announced, feature enabled and a single account`() = runTest {
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(aSessionData()),
                updateUserProfileResult = { _, _, _, _ -> },
            ),
            announcementService = FakeAnnouncementService(
                initialAnnouncementsToShowFlowValue = listOf(Announcement.MultiAccount),
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MultiAccount.key to true),
            ),
        )
        presenter.test {
            assertThat(awaitItem().showMultiAccountAnnouncement).isFalse()
            assertThat(expectMostRecentItem().showMultiAccountAnnouncement).isTrue()
        }
    }

    @Test
    fun `present - multi account announcement is not shown when the feature is disabled`() = runTest {
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(aSessionData()),
                updateUserProfileResult = { _, _, _, _ -> },
            ),
            announcementService = FakeAnnouncementService(
                initialAnnouncementsToShowFlowValue = listOf(Announcement.MultiAccount),
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MultiAccount.key to false),
            ),
        )
        presenter.test {
            assertThat(expectMostRecentItem().showMultiAccountAnnouncement).isFalse()
        }
    }

    @Test
    fun `present - multi account announcement is not shown and is dismissed when there are several accounts`() = runTest {
        val onAnnouncementDismissedResult = lambdaRecorder<Announcement, Unit> { }
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = "@alice:server.org"),
                    aSessionData(sessionId = "@bob:server.org"),
                ),
                updateUserProfileResult = { _, _, _, _ -> },
            ),
            announcementService = FakeAnnouncementService(
                initialAnnouncementsToShowFlowValue = listOf(Announcement.MultiAccount),
                onAnnouncementDismissedResult = onAnnouncementDismissedResult,
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MultiAccount.key to true),
            ),
        )
        presenter.test {
            assertThat(expectMostRecentItem().showMultiAccountAnnouncement).isFalse()
            onAnnouncementDismissedResult.assertions().isCalledOnce()
                .with(value(Announcement.MultiAccount))
        }
    }

    @Test
    fun `present - dismiss multi account announcement`() = runTest {
        val onAnnouncementDismissedResult = lambdaRecorder<Announcement, Unit> { }
        val announcementService = FakeAnnouncementService(
            initialAnnouncementsToShowFlowValue = listOf(Announcement.MultiAccount),
            onAnnouncementDismissedResult = onAnnouncementDismissedResult,
        )
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(aSessionData()),
                updateUserProfileResult = { _, _, _, _ -> },
            ),
            announcementService = announcementService,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MultiAccount.key to true),
            ),
        )
        presenter.test {
            val state = expectMostRecentItem()
            assertThat(state.showMultiAccountAnnouncement).isTrue()
            state.eventSink(HomeEvent.DismissMultiAccountAnnouncement)
            announcementService.emitAnnouncementsToShow(emptyList())
            assertThat(awaitItem().showMultiAccountAnnouncement).isFalse()
            onAnnouncementDismissedResult.assertions().isCalledOnce()
                .with(value(Announcement.MultiAccount))
        }
    }
}

internal fun createHomePresenter(
    client: MatrixClient = FakeMatrixClient(),
    syncService: SyncService = FakeSyncService(),
    snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(false) },
    indicatorService: IndicatorService = FakeIndicatorService(),
    homeSpacesPresenter: Presenter<HomeSpacesState> = Presenter { aHomeSpacesState() },
    sessionStore: SessionStore = InMemorySessionStore(),
    announcementService: AnnouncementService = FakeAnnouncementService(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
) = HomePresenter(
    client = client,
    syncService = syncService,
    snackbarDispatcher = snackbarDispatcher,
    indicatorService = indicatorService,
    roomListPresenter = { aRoomListState() },
    homeSpacesPresenter = homeSpacesPresenter,
    rageshakeFeatureAvailability = rageshakeFeatureAvailability,
    sessionStore = sessionStore,
    announcementService = announcementService,
    featureFlagService = featureFlagService,
)
