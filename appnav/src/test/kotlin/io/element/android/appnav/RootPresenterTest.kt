/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.root.RootPresenter
import io.element.android.features.rageshake.api.crash.aCrashDetectionState
import io.element.android.features.rageshake.api.detection.aRageshakeDetectionState
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import io.element.android.services.apperror.impl.DefaultAppErrorStateService
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.WarmUpRule
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
            val initialState = awaitItem()
            assertThat(initialState.crashDetectionState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - passes app error state`() = runTest {
        val presenter = createRootPresenter(
            appErrorService = DefaultAppErrorStateService(
                stringProvider = FakeStringProvider(),
            ).apply {
                showError("Bad news", "Something bad happened")
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        appErrorService: AppErrorStateService = DefaultAppErrorStateService(
            stringProvider = FakeStringProvider(),
        ),
    ): RootPresenter {
        return RootPresenter(
            crashDetectionPresenter = { aCrashDetectionState() },
            rageshakeDetectionPresenter = { aRageshakeDetectionState() },
            appErrorStateService = appErrorService,
            analyticsService = FakeAnalyticsService(),
            sdkMetadata = FakeSdkMetadata("sha")
        )
    }
}
