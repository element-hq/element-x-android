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

package example.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

data class MyState(
    val anInt: Int,
    val eventSink: (MyEvent) -> Unit,
)

sealed interface MyEvent {
    object Increment : MyEvent
    object IncrementSuspending : MyEvent
}

@Composable
fun myPresenter(): MyState {
    val scope = rememberCoroutineScope()
    val anInt: MutableState<Int> = remember { mutableStateOf(0) }
    return MyState(anInt.value) {
        when (it) {
            MyEvent.Increment -> {
                anInt.value++
                anInt.value++
            }
            MyEvent.IncrementSuspending -> scope.launch {
                anInt.value++
                anInt.value++
            }
        }
    }
}

class MoleculeTestCase {
    @Test
    fun `process Increment event`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) { myPresenter() }.test {
            awaitItem().apply {
                assertEquals(0, anInt)
                eventSink(MyEvent.Increment)
            }
            assertEquals(1, awaitItem().anInt)
            assertEquals(2, awaitItem().anInt)
        }
    }

    @Test
    fun `process IncrementSuspending event`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) { myPresenter() }.test {
            awaitItem().apply {
                assertEquals(0, anInt)
                eventSink(MyEvent.IncrementSuspending)
            }
            assertEquals(2, awaitItem().anInt)
        }
    }
}
