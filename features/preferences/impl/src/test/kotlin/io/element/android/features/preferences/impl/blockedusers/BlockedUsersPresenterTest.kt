/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.blockedusers

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BlockedUsersPresenterTest {
    @Test
    fun `present - initial state with no blocked users`() = runTest {
        val presenter = aBlockedUsersPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(blockedUsers).isEmpty()
                assertThat(unblockUserAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - initial state with blocked users`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID)
        }
        val presenter = aBlockedUsersPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(blockedUsers).isEqualTo(persistentListOf(MatrixUser(A_USER_ID)))
                assertThat(unblockUserAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - blocked users list updates with new emissions`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID)
        }
        val presenter = aBlockedUsersPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(blockedUsers).isEqualTo(listOf(MatrixUser(A_USER_ID)))
            }
            matrixClient.ignoredUsersFlow.value = persistentListOf(A_USER_ID, A_USER_ID_2)
            skipItems(1)
            with(awaitItem()) {
                assertThat(blockedUsers).isEqualTo(listOf(MatrixUser(A_USER_ID), MatrixUser(A_USER_ID_2)))
            }
        }
    }

    @Test
    fun `present - blocked users list with data`() = runTest {
        val alice = MatrixUser(A_USER_ID, displayName = "Alice", avatarUrl = "aliceAvatar")
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID, A_USER_ID_2)
            givenGetProfileResult(A_USER_ID, Result.success(alice))
            givenGetProfileResult(A_USER_ID_2, Result.failure(AN_EXCEPTION))
        }
        val presenter = aBlockedUsersPresenter(
            matrixClient = matrixClient,
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.ShowBlockedUsersDetails, true)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(blockedUsers).isEqualTo(listOf(MatrixUser(A_USER_ID), MatrixUser(A_USER_ID_2)))
            }
            // Alice is resolved
            with(awaitItem()) {
                assertThat(blockedUsers).isEqualTo(listOf(alice, MatrixUser(A_USER_ID_2)))
            }
        }
    }

    @Test
    fun `present - unblock user`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID)
        }
        val presenter = aBlockedUsersPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(BlockedUsersEvents.Unblock(A_USER_ID))

            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Confirming::class.java)
            initialState.eventSink(BlockedUsersEvents.ConfirmUnblock)

            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Success::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - unblock user handles failure`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID)
            givenUnignoreUserResult(Result.failure(IllegalStateException("User not banned")))
        }
        val presenter = aBlockedUsersPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(BlockedUsersEvents.Unblock(A_USER_ID))

            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Confirming::class.java)
            initialState.eventSink(BlockedUsersEvents.ConfirmUnblock)

            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Failure::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - unblock user then cancel`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            ignoredUsersFlow.value = persistentListOf(A_USER_ID)
            givenUnignoreUserResult(Result.failure(IllegalStateException("User not banned")))
        }
        val presenter = aBlockedUsersPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(BlockedUsersEvents.Unblock(A_USER_ID))

            assertThat(awaitItem().unblockUserAction).isInstanceOf(AsyncAction.Confirming::class.java)
            initialState.eventSink(BlockedUsersEvents.Cancel)

            assertThat(awaitItem().unblockUserAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - confirm unblock without a pending blocked user does nothing`() = runTest {
        val presenter = aBlockedUsersPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(BlockedUsersEvents.ConfirmUnblock)
            ensureAllEventsConsumed()
        }
    }

    private fun aBlockedUsersPresenter(
        buildMeta: BuildMeta = aBuildMeta(),
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
    ) = BlockedUsersPresenter(
        buildMeta = buildMeta,
        matrixClient = matrixClient,
        featureFlagService = featureFlagService,
    )
}
