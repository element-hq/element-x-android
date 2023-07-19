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

package io.element.android.libraries.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

/**
 * Create a child scope of the current scope.
 * The child scope will be cancelled if the parent scope is cancelled.
 * The child scope will be cancelled if an exception is thrown in the parent scope.
 * The parent scope won't be cancelled when an exception is thrown in the child scope.
 *
 * @param dispatcher the dispatcher to use for this scope.
 * @param name the name of the coroutine.
 */
fun CoroutineScope.childScope(
    dispatcher: CoroutineDispatcher,
    name: String,
): CoroutineScope = run {
    val supervisorJob = SupervisorJob(parent = coroutineContext.job)
    this + dispatcher + supervisorJob + CoroutineName(name)
}
