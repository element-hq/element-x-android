/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalPermissionsApi::class)

package io.element.android.libraries.permissions.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.impl.action.FakePermissionActions
import io.element.android.libraries.permissions.test.InMemoryPermissionsStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

const val A_PERMISSION = "A_PERMISSION"

class DefaultPermissionsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Granted
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.permission).isEqualTo(A_PERMISSION)
            assertThat(initialState.permissionGranted).isTrue()
            assertThat(initialState.shouldShowRationale).isFalse()
            assertThat(initialState.permissionAlreadyAsked).isFalse()
            assertThat(initialState.permissionAlreadyDenied).isFalse()
            assertThat(initialState.showDialog).isFalse()
        }
    }

    @Test
    fun `present - user closes dialog`() = runTest {
        val permissionsStore = InMemoryPermissionsStore(
            permissionDenied = true,
            permissionAsked = true
        )
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            withDialogState.eventSink.invoke(PermissionsEvents.CloseDialog)
            assertThat(awaitItem().showDialog).isFalse()
        }
    }

    @Test
    fun `present - user open settings`() = runTest {
        val permissionsStore = InMemoryPermissionsStore(
            permissionDenied = true,
            permissionAsked = true
        )
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val permissionActions = FakePermissionActions()
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            permissionActions,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            assertThat(permissionActions.openSettingsCalled).isFalse()
            withDialogState.eventSink.invoke(PermissionsEvents.OpenSystemSettingAndCloseDialog)
            assertThat(awaitItem().showDialog).isFalse()
            assertThat(permissionActions.openSettingsCalled).isTrue()
        }
    }

    @Test
    fun `present - user does not grant permission`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            // User does not grant permission
            permissionStateProvider.userGiveAnswer(answer = false, firstTime = true)
            skipItems(1)
            val state = awaitItem()
            assertThat(state.permissionGranted).isFalse()
            assertThat(state.showDialog).isFalse()
            assertThat(state.permissionAlreadyDenied).isFalse()
            assertThat(state.permissionAlreadyAsked).isTrue()
        }
    }

    @Test
    fun `present - user does not grant permission second time`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = true)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            // User does not grant permission
            permissionStateProvider.userGiveAnswer(answer = false, firstTime = false)
            skipItems(2)
            val state = awaitItem()
            assertThat(state.permissionGranted).isFalse()
            assertThat(state.showDialog).isFalse()
            assertThat(state.permissionAlreadyDenied).isTrue()
            assertThat(state.permissionAlreadyAsked).isTrue()
        }
    }

    @Test
    fun `present - user does not grant permission third time`() = runTest {
        val permissionsStore =
            InMemoryPermissionsStore(
                permissionDenied = true,
                permissionAsked = true
            )
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            assertThat(withDialogState.permissionGranted).isFalse()
            assertThat(withDialogState.permissionAlreadyDenied).isTrue()
            assertThat(withDialogState.permissionAlreadyAsked).isTrue()
        }
    }

    @Test
    fun `present - user grants permission`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(
            A_PERMISSION,
            PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider =
            FakeComposablePermissionStateProvider(
                permissionState
            )
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider,
            FakePermissionActions(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvents.RequestPermissions)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            // User grants permission
            permissionStateProvider.userGiveAnswer(answer = true, firstTime = true)
            skipItems(1)
            val state = awaitItem()
            assertThat(state.permissionGranted).isTrue()
            assertThat(state.showDialog).isFalse()
            assertThat(state.permissionAlreadyDenied).isFalse()
            assertThat(state.permissionAlreadyAsked).isTrue()
        }
    }
}
