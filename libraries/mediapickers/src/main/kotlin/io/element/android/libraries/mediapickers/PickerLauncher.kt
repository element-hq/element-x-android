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

package io.element.android.libraries.mediapickers

import androidx.activity.compose.ManagedActivityResultLauncher

/**
 * Wrapper around [ManagedActivityResultLauncher] to be used with media/file pickers.
 */
interface PickerLauncher<Input, Output> {
    /** Starts the activity result launcher with its default input. */
    fun launch()

    /** Starts the activity result launcher with a [customInput]. */
    fun launch(customInput: Input)
}

class ComposePickerLauncher<Input, Output>(
    private val managedLauncher: ManagedActivityResultLauncher<Input, Output>,
    private val defaultRequest: Input,
) : PickerLauncher<Input, Output> {
    override fun launch() {
        managedLauncher.launch(defaultRequest)
    }

    override fun launch(customInput: Input) {
        managedLauncher.launch(customInput)
    }
}

/** Needed for screenshot tests. */
class NoOpPickerLauncher<Input, Output>(
    private val onResult: () -> Unit,
) : PickerLauncher<Input, Output> {
    override fun launch() = onResult()
    override fun launch(customInput: Input) = onResult()
}
