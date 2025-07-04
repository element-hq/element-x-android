/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.roomlist.aRoomListState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
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
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.TestScope
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
            rageshakeFeatureAvailability = { false },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isEqualTo(MatrixUser(A_USER_ID))
            assertThat(initialState.canReportBug).isFalse()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser.userId).isEqualTo(A_USER_ID)
            assertThat(withUserState.matrixUser.displayName).isEqualTo(A_USER_NAME)
            assertThat(withUserState.matrixUser.avatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(withUserState.showAvatarIndicator).isFalse()
            assertThat(withUserState.isSpaceFeatureEnabled).isFalse()
        }
    }

    @Test
    fun `present - space feature enabled`() = runTest {
        val presenter = createHomePresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Space.key to true),
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
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val presenter = createHomePresenter(client = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isEqualTo(MatrixUser(matrixClient.sessionId))
            // No new state is coming
        }
    }

    @Test
    fun `present - NavigationBar change`() = runTest {
        val presenter = createHomePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            initialState.eventSink(HomeEvents.SelectHomeNavigationBarItem(HomeNavigationBarItem.Spaces))
            val finalState = awaitItem()
            assertThat(finalState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Spaces)
        }
    }

    private fun TestScope.createHomePresenter(
        client: MatrixClient = FakeMatrixClient(),
        syncService: SyncService = FakeSyncService(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { true },
        indicatorService: IndicatorService = FakeIndicatorService(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService()
    ) = HomePresenter(
        client = client,
        syncService = syncService,
        snackbarDispatcher = snackbarDispatcher,
        indicatorService = indicatorService,
        logoutPresenter = { aDirectLogoutState() },
        roomListPresenter = { aRoomListState() },
        rageshakeFeatureAvailability = rageshakeFeatureAvailability,
        featureFlagService = featureFlagService,
    )
}
