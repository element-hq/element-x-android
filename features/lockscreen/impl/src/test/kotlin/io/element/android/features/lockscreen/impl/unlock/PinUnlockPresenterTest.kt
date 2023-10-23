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

package io.element.android.features.lockscreen.impl.unlock

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.pin.model.assertEmpty
import io.element.android.features.lockscreen.impl.pin.model.assertText
import io.element.android.features.lockscreen.impl.state.DefaultLockScreenStateService
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinUnlockPresenterTest {

    private val halfCompletePin = "12"
    private val completePin = "1235"

    @Test
    fun `present - complete flow`() = runTest {
        val presenter = createPinUnlockPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                state.pinEntry.assertEmpty()
                assertThat(state.showWrongPinTitle).isFalse()
                assertThat(state.showSignOutPrompt).isFalse()
                assertThat(state.remainingAttempts).isEqualTo(3)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('2')))
            }
            awaitLastSequentialItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Back))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Empty))
            }
            awaitLastSequentialItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
                state.eventSink(PinUnlockEvents.OnForgetPin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showSignOutPrompt).isEqualTo(true)
                assertThat(state.isSignOutPromptCancellable).isEqualTo(true)
                state.eventSink(PinUnlockEvents.ClearSignOutPrompt)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showSignOutPrompt).isEqualTo(false)
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvents.OnPinKeypadPressed(PinKeypadModel.Number('5')))
            }
            awaitLastSequentialItem().also { state ->
                state.pinEntry.assertText(completePin)
            }
        }
    }

    private suspend fun createPinUnlockPresenter(scope: CoroutineScope): PinUnlockPresenter {
        val featureFlagService = FakeFeatureFlagService().apply {
            setFeatureEnabled(FeatureFlags.PinUnlock, true)
        }
        val lockScreenStateService = DefaultLockScreenStateService(featureFlagService)
        return PinUnlockPresenter(
            lockScreenStateService,
            scope,
        )
    }
}
