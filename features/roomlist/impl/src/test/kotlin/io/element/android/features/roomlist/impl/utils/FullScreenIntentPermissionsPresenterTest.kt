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

package io.element.android.features.roomlist.impl.utils

import android.content.Intent
import android.os.Build
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import io.element.android.services.toolbox.test.intent.FakeExternalIntentLauncher
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FullScreenIntentPermissionsPresenterTest {
    @Test
    fun `shouldDisplay - is true when permission is not granted and banner is not dismissed`() = runTest {
        val presenter = createPresenter(
            permissionsPresenter = FakePermissionsPresenter().apply {
                setPermissionDenied()
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialItem = awaitItem()
            assertThat(initialItem.shouldDisplay).isTrue()
        }
    }

    @Test
    fun `shouldDisplay - is false on old APIs`() = runTest {
        val presenter = createPresenter(
            permissionsPresenter = FakePermissionsPresenter().apply {
                setPermissionDenied()
            },
            buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.Q)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialItem = awaitItem()
            assertThat(initialItem.shouldDisplay).isFalse()
        }
    }

    @Test
    fun `shouldDisplay - is false if permission is granted`() = runTest {
        val presenter = createPresenter(
            permissionsPresenter = FakePermissionsPresenter().apply {
                setPermissionGranted()
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialItem = awaitItem()
            assertThat(initialItem.shouldDisplay).isFalse()
        }
    }

    @Test
    fun `dismissFullScreenIntentBanner - makes shouldDisplay false`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedItem = awaitItem()
            loadedItem.dismissFullScreenIntentBanner()

            runCurrent()

            assertThat(awaitItem().shouldDisplay).isFalse()
        }
    }

    @Test
    fun `openFullScreenIntentSettings - opens external screen using intent`() = runTest {
        val launchLambda = lambdaRecorder<Intent, Unit> { _ -> }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val presenter = createPresenter(externalIntentLauncher = externalIntentLauncher)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedItem = awaitItem()
            loadedItem.openFullScreenIntentSettings()

            launchLambda.assertions().isCalledOnce()

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPresenter(
        buildVersionSdkIntProvider: FakeBuildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.UPSIDE_DOWN_CAKE),
        dataStoreFactory: FakePreferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
        externalIntentLauncher: ExternalIntentLauncher = FakeExternalIntentLauncher(),
        buildMeta: BuildMeta = aBuildMeta(),
        permissionsPresenter: FakePermissionsPresenter = FakePermissionsPresenter(),
    ) = FullScreenIntentPermissionsPresenter(
        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
        externalIntentLauncher = externalIntentLauncher,
        buildMeta = buildMeta,
        preferencesDataStoreFactory = dataStoreFactory,
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionPresenter = permissionsPresenter),
    )
}
