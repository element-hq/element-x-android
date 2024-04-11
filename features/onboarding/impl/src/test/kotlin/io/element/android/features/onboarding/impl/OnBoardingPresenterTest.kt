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

package io.element.android.features.onboarding.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnBoardingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = OnBoardingPresenter(
            aBuildMeta(
                applicationName = "A",
                productionApplicationName = "B",
                desktopApplicationName = "C",
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isDebugBuild).isTrue()
            assertThat(initialState.productionApplicationName).isEqualTo("B")
            assertThat(initialState.canLoginWithQrCode).isFalse()
            assertThat(initialState.canCreateAccount).isFalse()
        }
    }

    @Test
    fun `present - initial state release`() = runTest {
        val presenter = OnBoardingPresenter(aBuildMeta(buildType = BuildType.RELEASE))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isDebugBuild).isFalse()
        }
    }
}
