/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.screens.qrcode.intro

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QrCodeIntroPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createQrCodeIntroPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().run {
                assertThat(appName).isEqualTo("AppName")
                assertThat(desktopAppName).isEqualTo("DesktopAppName")
                assertThat(cameraPermissionState.permission).isEqualTo("android.permission.POST_NOTIFICATIONS")
                assertThat(canContinue).isFalse()
            }
        }
    }

    @Test
    fun `present - Continue with camera permissions can continue`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val presenter = createQrCodeIntroPresenter(permissionsPresenterFactory = permissionsPresenterFactory)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(QrCodeIntroEvents.Continue)
            assertThat(awaitItem().canContinue).isTrue()
        }
    }

    @Test
    fun `present - Continue with unknown camera permissions opens permission dialog`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter()
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val presenter = createQrCodeIntroPresenter(permissionsPresenterFactory = permissionsPresenterFactory)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(QrCodeIntroEvents.Continue)
            assertThat(awaitItem().cameraPermissionState.showDialog).isTrue()
        }
    }

    private fun createQrCodeIntroPresenter(
        buildMeta: BuildMeta = aBuildMeta(
            applicationName = "AppName",
            desktopApplicationName = "DesktopAppName",
        ),
        permissionsPresenterFactory: FakePermissionsPresenterFactory = FakePermissionsPresenterFactory(),
    ): QrCodeIntroPresenter {
        return QrCodeIntroPresenter(
            buildMeta = buildMeta,
            permissionsPresenterFactory = permissionsPresenterFactory,
        )
    }
}
