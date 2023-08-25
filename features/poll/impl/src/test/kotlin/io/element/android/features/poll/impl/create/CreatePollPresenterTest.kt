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

import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreatePollPresenterTest {

    // TODO: Build presenter with fake deps here.

    fun `default state has empty question with 2 empty answers and disclosed kind`() = runTest {
    }

    @Test
    fun `non blank question and 2 answers are required to create a poll`() = runTest {
    }

    fun `create polls sends a poll start event`() = runTest {
    }

    fun `add answer button adds an empty answer`() = runTest {
    }

    fun `delete answer button removes the given answer`() = runTest {
    }

    fun `set question sets the question`() = runTest {
    }

    fun `set poll answer sets the given poll answer`() = runTest {
    }

    fun `set poll kind sets the poll kind`() = runTest {
    }

    fun `can add options when between 2 and 20`() = runTest {
    }

    fun `cannot add option when there are already 20`() = runTest {
    }

    fun `can delete option if there are more than 2`() = runTest {
    }

    fun `option with more than 240 char is truncated`() = runTest {
    }
}
