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

package io.element.android.libraries.textcomposer.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.textcomposer.utils.PressState.Idle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class) class PressStateHolderTest {
    companion object {
        const val LONG_PRESS_TIMEOUT_MILLIS = 1L
    }
    @Test
    fun `it starts in idle state`() = runTest {
        val stateHolder = createStateHolder()
        assertThat(stateHolder.value).isEqualTo(Idle(lastPress = null))
    }

    @Test
    fun `when press, it moves to tapping state`() = runTest {
        val stateHolder = createStateHolder()
        val press = async { stateHolder.press() }
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.Tapping)
        press.await()
    }

    @Test
    fun `when release after short delay, it moves through tap states`() = runTest {
        val stateHolder = createStateHolder()
        val press = async { stateHolder.press() }
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.Tapping)
        stateHolder.release()
        advanceTimeBy(1.milliseconds) // wait for the long press timeout which should not be triggered
        assertThat(stateHolder.value).isEqualTo(Idle(lastPress = PressState.Tapping))
        press.await()
    }

    @Test
    fun `when hold, it moves through long press states`() = runTest {
        val stateHolder = createStateHolder()
        val press = async { stateHolder.press() }
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.Tapping)
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.LongPressing)
        stateHolder.release()
        assertThat(stateHolder.value).isEqualTo(Idle(lastPress = PressState.LongPressing))
        press.await()
    }

    @Test
    fun `when release and repress, it doesn't enter long press states`() = runTest {
        val stateHolder = createStateHolder()
        val press1 = async { stateHolder.press() }
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.Tapping)
        stateHolder.release()
        val press2 = async { stateHolder.press() }
        advanceTimeBy(1.milliseconds)
        assertThat(stateHolder.value).isEqualTo(PressState.Tapping)
        press1.await()
        press2.await()
    }

    @Test
    fun `when press twice without releasing, it doesn't throw an error`() = runTest {
        val stateHolder = createStateHolder()
        stateHolder.press()
        stateHolder.press()
    }

    @Test
    fun `when release without first pressing, it doesn't throw an error`() = runTest {
        val stateHolder = createStateHolder()
        stateHolder.release()
    }

    @Test
    fun `when release twice without pressing, it doesn't throw an error `() = runTest {
        val stateHolder = createStateHolder()
        stateHolder.press()
        stateHolder.release()
        stateHolder.release()
    }

    private fun createStateHolder() =
        PressStateHolder(
            LONG_PRESS_TIMEOUT_MILLIS,
        )
}
