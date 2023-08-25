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

package io.element.android.features.poll.impl.create

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.services.analytics.noop.NoopAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreatePollPresenterTest {

    val presenter = CreatePollPresenter(
        room = FakeMatrixRoom(),
        analyticsService = NoopAnalyticsService(),
    )

    @Test
    fun `default state has empty question with 2 empty answers and disclosed kind`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let {
                Truth.assertThat(it.canCreate).isEqualTo(false)
                Truth.assertThat(it.canAddAnswer).isEqualTo(true)
                Truth.assertThat(it.question).isEqualTo("")
                Truth.assertThat(it.answers).isEqualTo(listOf(Answer("", false), Answer("", false)))
                Truth.assertThat(it.pollKind).isEqualTo(PollKind.Disclosed)
            }
        }
    }

    @Test
    fun `non blank question and 2 answers are required to create a poll`() = runTest {
    }

    @Test
    fun `create polls sends a poll start event`() = runTest {
    }

    @Test
    fun `add answer button adds an empty answer`() = runTest {
    }

    @Test
    fun `delete answer button removes the given answer`() = runTest {
    }

    @Test
    fun `set question sets the question`() = runTest {
    }

    @Test
    fun `set poll answer sets the given poll answer`() = runTest {
    }

    @Test
    fun `set poll kind sets the poll kind`() = runTest {
    }

    @Test
    fun `can add options when between 2 and 20`() = runTest {
    }

    @Test
    fun `cannot add option when there are already 20`() = runTest {
    }

    @Test
    fun `can delete option if there are more than 2`() = runTest {
    }

    @Test
    fun `option with more than 240 char is truncated`() = runTest {
    }
}
