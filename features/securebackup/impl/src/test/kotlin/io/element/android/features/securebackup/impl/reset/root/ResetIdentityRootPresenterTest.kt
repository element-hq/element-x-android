/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.DirectLogoutEvent
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.EventsRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.variant).isEqualTo(ResetIdentityRootVariant.HasConfirmationOptions)
            assertThat(initialState.displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - Continue event displays the confirmation dialog`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)

            assertThat(awaitItem().displayConfirmationDialog).isTrue()
        }
    }

    @Test
    fun `present - DismissDialog event hides the confirmation dialog`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)
            assertThat(awaitItem().displayConfirmationDialog).isTrue()

            initialState.eventSink(ResetIdentityRootEvent.DismissDialog)
            assertThat(awaitItem().displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - without other options and with encrypted chats`() = runTest {
        val presenter = createPresenter(
            hasConfirmationOptions = false,
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.success(true) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().variant).isNull()
            assertThat(awaitItem().variant).isEqualTo(ResetIdentityRootVariant.NoOptionsWithEncryptedChats)
        }
    }

    @Test
    fun `present - without other options and without encrypted chats`() = runTest {
        val presenter = createPresenter(
            hasConfirmationOptions = false,
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.success(false) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().variant).isNull()
            assertThat(awaitItem().variant).isEqualTo(ResetIdentityRootVariant.NoOptionsWithoutEncryptedChats)
        }
    }

    @Test
    fun `present - without other options, failing to know about the chats is treated as having encrypted chats`() = runTest {
        val presenter = createPresenter(
            hasConfirmationOptions = false,
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.failure(IllegalStateException("Failed")) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().variant).isNull()
            assertThat(awaitItem().variant).isEqualTo(ResetIdentityRootVariant.NoOptionsWithEncryptedChats)
        }
    }

    @Test
    fun `present - with other options the encrypted chats are not checked`() = runTest {
        val presenter = createPresenter(
            hasConfirmationOptions = true,
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { error("Should not be called") }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().variant).isEqualTo(ResetIdentityRootVariant.HasConfirmationOptions)
        }
    }

    @Test
    fun `present - SignOut event logs out`() = runTest {
        val logoutEventRecorder = EventsRecorder<DirectLogoutEvent>()
        val presenter = createPresenter(
            directLogoutPresenter = { aDirectLogoutState(eventSink = logoutEventRecorder) },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(ResetIdentityRootEvent.SignOut)

            logoutEventRecorder.assertSingle(DirectLogoutEvent.Logout(ignoreSdkError = false, skipConfirmation = true))
        }
    }

    private fun createPresenter(
        hasConfirmationOptions: Boolean = true,
        matrixClient: MatrixClient = FakeMatrixClient(),
        directLogoutPresenter: Presenter<DirectLogoutState> = Presenter { aDirectLogoutState() },
    ) = ResetIdentityRootPresenter(
        hasConfirmationOptions = hasConfirmationOptions,
        matrixClient = matrixClient,
        directLogoutPresenter = directLogoutPresenter,
    )
}
