/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediapickers.api

import android.content.ActivityNotFoundException
import androidx.activity.compose.ManagedActivityResultLauncher
import timber.log.Timber

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
        try {
            managedLauncher.launch(defaultRequest)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Timber.w(activityNotFoundException, "No activity found")
        }
    }

    override fun launch(customInput: Input) {
        try {
            managedLauncher.launch(customInput)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Timber.w(activityNotFoundException, "No activity found")
        }
    }
}

/** Needed for screenshot tests. */
class NoOpPickerLauncher<Input, Output>(
    private val onResult: () -> Unit,
) : PickerLauncher<Input, Output> {
    override fun launch() = onResult()
    override fun launch(customInput: Input) = onResult()
}
