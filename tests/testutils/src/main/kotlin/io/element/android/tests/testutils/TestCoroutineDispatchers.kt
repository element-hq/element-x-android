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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.tests.testutils

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

fun testCoroutineDispatchers(
    testScheduler: TestCoroutineScheduler? = null,
) = CoroutineDispatchers(
    io = UnconfinedTestDispatcher(testScheduler),
    computation = UnconfinedTestDispatcher(testScheduler),
    main = UnconfinedTestDispatcher(testScheduler),
    diffUpdateDispatcher = UnconfinedTestDispatcher(testScheduler),
)

fun testCoroutineDispatchers(
    io: TestDispatcher = UnconfinedTestDispatcher(),
    computation: TestDispatcher = UnconfinedTestDispatcher(),
    main: TestDispatcher = UnconfinedTestDispatcher(),
    diffUpdateDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = CoroutineDispatchers(
    io = io,
    computation = computation,
    main = main,
    diffUpdateDispatcher = diffUpdateDispatcher,
)
