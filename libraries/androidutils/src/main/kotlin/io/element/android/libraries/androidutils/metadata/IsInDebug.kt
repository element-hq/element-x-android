/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.androidutils.metadata

import io.element.android.libraries.androidutils.BuildConfig

/**
 * true if the app is built in debug mode.
 * For testing purpose, this can be changed with [withReleaseBehavior].
 */
var isInDebug: Boolean = BuildConfig.DEBUG
    private set

/**
 * Run the lambda simulating the app is in release mode.
 *
 * **IMPORTANT**: this should **ONLY** be used for testing purposes.
 */
fun withReleaseBehavior(lambda: () -> Unit) {
    isInDebug = false
    lambda()
    isInDebug = BuildConfig.DEBUG
}
