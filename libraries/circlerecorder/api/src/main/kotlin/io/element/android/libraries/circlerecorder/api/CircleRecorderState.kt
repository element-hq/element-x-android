/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.circlerecorder.api

import androidx.compose.runtime.Immutable
import java.io.File
import kotlin.time.Duration

@Immutable
sealed interface CircleRecorderState {
    data object Idle : CircleRecorderState

    data class Recording(
        val elapsedTime: Duration,
        val useFrontCamera: Boolean,
        val isSwitchingCamera: Boolean = false,
    ) : CircleRecorderState

    data class Finished(
        val file: File,
        val mimeType: String,
        val duration: Duration,
    ) : CircleRecorderState
}
