/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.desktop

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DesktopNoticePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            awaitItem().run {
                assertThat(cameraPermissionState.permission).isEqualTo("android.permission.POST_NOTIFICATIONS")
                assertThat(canContinue).isFalse()
            }
        }
    }

    @Test
    fun `present - Continue with camera permissions can continue`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val presenter = createPresenter(permissionsPresenterFactory = permissionsPresenterFactory)
        presenter.test {
            awaitItem().eventSink(DesktopNoticeEvent.Continue)
            assertThat(awaitItem().canContinue).isTrue()
        }
    }

    @Test
    fun `present - Continue with unknown camera permissions opens permission dialog`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter()
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val presenter = createPresenter(permissionsPresenterFactory = permissionsPresenterFactory)
        presenter.test {
            awaitItem().eventSink(DesktopNoticeEvent.Continue)
            assertThat(awaitItem().cameraPermissionState.showDialog).isTrue()
        }
    }
}

private fun createPresenter(
    permissionsPresenterFactory: FakePermissionsPresenterFactory = FakePermissionsPresenterFactory(),
) = DesktopNoticePresenter(
    permissionsPresenterFactory = permissionsPresenterFactory,
)
