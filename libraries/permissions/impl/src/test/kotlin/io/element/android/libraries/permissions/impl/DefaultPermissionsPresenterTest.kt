/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalPermissionsApi::class)

package io.element.android.libraries.permissions.impl

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsStore
import io.element.android.libraries.permissions.impl.action.FakePermissionActions
import io.element.android.libraries.permissions.impl.action.PermissionActions
import io.element.android.libraries.permissions.test.InMemoryPermissionsStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val A_PERMISSION = "A_PERMISSION"

class DefaultPermissionsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
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
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = aFakePermissionState(
                initialStatus = PermissionStatus.Denied(shouldShowRationale = false)
            ),
        )
        val presenter = createPresenter(
            permissionsStore = permissionsStore,
            permissionStateProvider = permissionStateProvider,
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            withDialogState.eventSink.invoke(PermissionsEvent.CloseDialog)
            assertThat(awaitItem().showDialog).isFalse()
        }
    }

    @Test
    fun `present - user open settings`() = runTest {
        val permissionsStore = InMemoryPermissionsStore(
            permissionDenied = true,
            permissionAsked = true
        )
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = aFakePermissionState(
                initialStatus = PermissionStatus.Denied(shouldShowRationale = false),
            ),
        )
        val openSettingsAction = lambdaRecorder<String, Unit> { }
        val permissionActions = FakePermissionActions(
            openSettingsAction = openSettingsAction,
        )
        val presenter = createPresenter(
            permissionsStore = permissionsStore,
            permissionStateProvider = permissionStateProvider,
            permissionActions = permissionActions,
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            openSettingsAction.assertions().isNeverCalled()
            withDialogState.eventSink.invoke(PermissionsEvent.OpenSystemSettingAndCloseDialog)
            assertThat(awaitItem().showDialog).isFalse()
            openSettingsAction.assertions().isCalledOnce().with(value(A_PERMISSION))
        }
    }

    @Test
    fun `present - user does not grant permission`() = runTest {
        val permissionState = aFakePermissionState(
            initialStatus = PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = permissionState,
        )
        val presenter = createPresenter(
            permissionStateProvider = permissionStateProvider,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
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
        val permissionState = aFakePermissionState(
            initialStatus = PermissionStatus.Denied(shouldShowRationale = true)
        )
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = permissionState,
        )
        val presenter = createPresenter(
            permissionStateProvider = permissionStateProvider,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
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
        val permissionsStore = InMemoryPermissionsStore(
            permissionDenied = true,
            permissionAsked = true,
        )
        val permissionState = aFakePermissionState(
            initialStatus = PermissionStatus.Denied(shouldShowRationale = false),
        )
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = permissionState,
        )
        val presenter = createPresenter(
            permissionsStore = permissionsStore,
            permissionStateProvider = permissionStateProvider,
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
            val withDialogState = awaitItem()
            assertThat(withDialogState.showDialog).isTrue()
            assertThat(withDialogState.permissionGranted).isFalse()
            assertThat(withDialogState.permissionAlreadyDenied).isTrue()
            assertThat(withDialogState.permissionAlreadyAsked).isTrue()
        }
    }

    @Test
    fun `present - user grants permission`() = runTest {
        val permissionState = aFakePermissionState(
            initialStatus = PermissionStatus.Denied(shouldShowRationale = false)
        )
        val permissionStateProvider = FakeComposablePermissionStateProvider(
            permissionState = permissionState,
        )
        val presenter = createPresenter(
            permissionStateProvider = permissionStateProvider,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isFalse()
            initialState.eventSink.invoke(PermissionsEvent.RequestPermissions)
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

private fun createPresenter(
    permission: String = A_PERMISSION,
    permissionsStore: PermissionsStore = InMemoryPermissionsStore(),
    permissionStateProvider: ComposablePermissionStateProvider = FakeComposablePermissionStateProvider(
        permissionState = aFakePermissionState(),
    ),
    permissionActions: PermissionActions = FakePermissionActions(),
) = DefaultPermissionsPresenter(
    permission = permission,
    permissionsStore = permissionsStore,
    composablePermissionStateProvider = permissionStateProvider,
    permissionActions = permissionActions,
)

private fun aFakePermissionState(
    permission: String = A_PERMISSION,
    initialStatus: PermissionStatus = PermissionStatus.Granted,
) = FakePermissionState(
    permission = permission,
    initialStatus = initialStatus,
)
