/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalPermissionsApi::class)

package io.element.android.libraries.permissions.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.api.PermissionsEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

const val A_PERMISSION = "A_PERMISSION"

class DefaultPermissionsPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Granted)
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Denied(shouldShowRationale = false))
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
            initialState.eventSink.invoke(PermissionsEvents.CloseDialog)
            assertThat(awaitItem().showDialog).isFalse()
        }
    }

    @Test
    fun `present - user does not grant permission`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Denied(shouldShowRationale = false))
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
            initialState.eventSink.invoke(PermissionsEvents.OpenSystemDialog)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            assertThat(awaitItem().showDialog).isFalse()
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
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Denied(shouldShowRationale = true))
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
            initialState.eventSink.invoke(PermissionsEvents.OpenSystemDialog)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            assertThat(awaitItem().showDialog).isFalse()
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
        val permissionsStore = InMemoryPermissionsStore(permissionDenied = true, permissionAsked = true)
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Denied(shouldShowRationale = false))
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
            assertThat(initialState.permissionGranted).isFalse()
            assertThat(initialState.permissionAlreadyDenied).isTrue()
            assertThat(initialState.permissionAlreadyAsked).isTrue()
        }
    }

    @Test
    fun `present - user grants permission`() = runTest {
        val permissionsStore = InMemoryPermissionsStore()
        val permissionState = FakePermissionState(A_PERMISSION, PermissionStatus.Denied(shouldShowRationale = false))
        val permissionStateProvider = FakePermissionStateProvider(permissionState)
        val presenter = DefaultPermissionsPresenter(
            A_PERMISSION,
            permissionsStore,
            permissionStateProvider
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
            initialState.eventSink.invoke(PermissionsEvents.OpenSystemDialog)
            assertThat(permissionState.launchPermissionRequestCalled).isTrue()
            assertThat(awaitItem().showDialog).isFalse()
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
