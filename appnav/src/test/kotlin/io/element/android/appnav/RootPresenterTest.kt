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

package io.element.android.appnav

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.root.RootPresenter
import io.element.android.features.rageshake.impl.crash.DefaultCrashDetectionPresenter
import io.element.android.features.rageshake.impl.detection.DefaultRageshakeDetectionPresenter
import io.element.android.features.rageshake.impl.preferences.DefaultRageshakePreferencesPresenter
import io.element.android.features.rageshake.test.crash.FakeCrashDataStore
import io.element.android.features.rageshake.test.rageshake.FakeRageShake
import io.element.android.features.rageshake.test.rageshake.FakeRageshakeDataStore
import io.element.android.features.rageshake.test.screenshot.FakeScreenshotHolder
import io.element.android.features.share.api.ShareService
import io.element.android.features.share.test.FakeShareService
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import io.element.android.services.apperror.impl.DefaultAppErrorStateService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetectionState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - check that share service is invoked`() = runTest {
        val lambda = lambdaRecorder<CoroutineScope, Unit> { _ -> }
        val presenter = createRootPresenter(
            shareService = FakeShareService {
                lambda(it)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            lambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - passes app error state`() = runTest {
        val presenter = createRootPresenter(
            appErrorService = DefaultAppErrorStateService().apply {
                showError("Bad news", "Something bad happened")
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            val initialState = awaitItem()
            assertThat(initialState.errorState).isInstanceOf(AppErrorState.Error::class.java)
            val initialErrorState = initialState.errorState as AppErrorState.Error
            assertThat(initialErrorState.title).isEqualTo("Bad news")
            assertThat(initialErrorState.body).isEqualTo("Something bad happened")

            initialErrorState.dismiss()
            assertThat(awaitItem().errorState).isInstanceOf(AppErrorState.NoError::class.java)
        }
    }

    private fun createRootPresenter(
        appErrorService: AppErrorStateService = DefaultAppErrorStateService(),
        shareService: ShareService = FakeShareService {},
    ): RootPresenter {
        val crashDataStore = FakeCrashDataStore()
        val rageshakeDataStore = FakeRageshakeDataStore()
        val rageshake = FakeRageShake()
        val screenshotHolder = FakeScreenshotHolder()
        val crashDetectionPresenter = DefaultCrashDetectionPresenter(
            buildMeta = aBuildMeta(),
            crashDataStore = crashDataStore
        )
        val rageshakeDetectionPresenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        return RootPresenter(
            crashDetectionPresenter = crashDetectionPresenter,
            rageshakeDetectionPresenter = rageshakeDetectionPresenter,
            appErrorStateService = appErrorService,
            analyticsService = FakeAnalyticsService(),
            shareService = shareService,
            sdkMetadata = FakeSdkMetadata("sha")
        )
    }
}
