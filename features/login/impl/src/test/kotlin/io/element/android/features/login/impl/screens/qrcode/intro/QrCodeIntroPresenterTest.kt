/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QrCodeIntroPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createQrCodeIntroPresenter()
        presenter.test {
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
        presenter.test {
            awaitItem().eventSink(QrCodeIntroEvents.Continue)
            assertThat(awaitItem().canContinue).isTrue()
        }
    }

    @Test
    fun `present - Continue with unknown camera permissions opens permission dialog`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter()
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val presenter = createQrCodeIntroPresenter(permissionsPresenterFactory = permissionsPresenterFactory)
        presenter.test {
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
