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

package io.element.android.features.lockscreen.impl.create

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.create.model.PinDigit
import io.element.android.features.lockscreen.impl.create.model.PinEntry
import io.element.android.features.lockscreen.impl.create.validation.CreatePinFailure
import io.element.android.features.lockscreen.impl.create.validation.PinValidator
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreatePinPresenterTest {

    private val blacklistedPin = PinValidator.BLACKLIST.first()
    private val halfCompletePin = "12"
    private val completePin = "1235"
    private val mismatchedPin = "1236"

    @Test
    fun `present - complete flow`() = runTest {

        val presenter = createCreatePinPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.createPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(halfCompletePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(halfCompletePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.createPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(blacklistedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(blacklistedPin)
                assertThat(state.createPinFailure).isEqualTo(CreatePinFailure.PinBlacklisted)
                state.eventSink(CreatePinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                assertThat(state.createPinFailure).isNull()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(completePin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(mismatchedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(mismatchedPin)
                assertThat(state.createPinFailure).isEqualTo(CreatePinFailure.PinsDontMatch)
                state.eventSink(CreatePinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isFalse()
                assertThat(state.createPinFailure).isNull()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(completePin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(CreatePinEvents.OnPinEntryChanged(completePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(completePin)
            }
        }
    }

    private fun PinEntry.assertText(text: String) {
        assertThat(toText()).isEqualTo(text)
    }

    private fun PinEntry.assertEmpty() {
        val isEmpty = digits.all { it is PinDigit.Empty }
        assertThat(isEmpty).isTrue()
    }

    private fun createCreatePinPresenter(): CreatePinPresenter {
        return CreatePinPresenter(PinValidator())
    }
}
