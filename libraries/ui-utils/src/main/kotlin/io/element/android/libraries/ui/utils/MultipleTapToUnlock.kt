/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Returns true if the user has tapped [numberOfTapToUnlock] times in a short amount of time.
 * The counter is reset after 2 seconds of inactivity.
 *
 * @param numberOfTapToUnlock The number of taps required to unlock.
 */
class MultipleTapToUnlock(
    private val numberOfTapToUnlock: Int = 7,
) {
    private var counter = numberOfTapToUnlock
    private var currentJob: Job? = null

    fun unlock(scope: CoroutineScope): Boolean {
        counter--
        currentJob?.cancel()
        return if (counter > 0) {
            currentJob = scope.launch {
                delay(2.seconds)
                // Reset counter if user is not fast enough
                counter = numberOfTapToUnlock
            }
            false
        } else {
            true
        }
    }
}
