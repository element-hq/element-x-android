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

package io.element.android.features.ftue.impl.notifications

import android.os.Build
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class NotificationsOptInPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private var isFinished = false

    @Test
    fun `initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.notificationsPermissionState.showDialog).isFalse()
        }
    }

    @Test
    fun `show dialog on continue clicked`() = runTest {
        val permissionPresenter = FakePermissionsPresenter()
        val presenter = createPresenter(permissionPresenter)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(NotificationsOptInEvents.ContinueClicked)
            assertThat(awaitItem().notificationsPermissionState.showDialog).isTrue()
        }
    }

    @Test
    fun `finish flow on continue clicked with permission already granted`() = runTest {
        val permissionPresenter = FakePermissionsPresenter().apply {
            setPermissionGranted()
        }
        val presenter = createPresenter(permissionPresenter)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(NotificationsOptInEvents.ContinueClicked)
            assertThat(isFinished).isTrue()
        }
    }

    @Test
    fun `finish flow on not now clicked`() = runTest {
        val permissionPresenter = FakePermissionsPresenter()
        val presenter = createPresenter(
            permissionsPresenter = permissionPresenter,
            sdkIntVersion = Build.VERSION_CODES.M
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(NotificationsOptInEvents.NotNowClicked)
            assertThat(isFinished).isTrue()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set permission denied on not now clicked in API 33`() = runTest(StandardTestDispatcher()) {
        val permissionPresenter = FakePermissionsPresenter()
        val permissionStateProvider = FakePermissionStateProvider()
        val presenter = createPresenter(
            permissionsPresenter = permissionPresenter,
            permissionStateProvider = permissionStateProvider,
            sdkIntVersion = Build.VERSION_CODES.TIRAMISU
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(NotificationsOptInEvents.NotNowClicked)

            // Allow background coroutines to run
            runCurrent()

            val isPermissionDenied = runBlocking {
                permissionStateProvider.isPermissionDenied("notifications").first()
            }
            assertThat(isPermissionDenied).isTrue()
        }
    }

    private fun TestScope.createPresenter(
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        permissionStateProvider: PermissionStateProvider = FakePermissionStateProvider(),
        sdkIntVersion: Int = Build.VERSION_CODES.TIRAMISU,
    ) = NotificationsOptInPresenter(
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
        callback = object : NotificationsOptInNode.Callback {
            override fun onNotificationsOptInFinished() {
                isFinished = true
            }
        },
        appCoroutineScope = this,
        permissionStateProvider = permissionStateProvider,
        buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkIntVersion),
    )
}
