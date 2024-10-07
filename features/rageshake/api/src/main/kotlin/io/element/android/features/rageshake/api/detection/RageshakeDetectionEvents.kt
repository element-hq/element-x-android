/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.detection

import io.element.android.features.rageshake.api.screenshot.ImageResult

sealed interface RageshakeDetectionEvents {
    data object Dismiss : RageshakeDetectionEvents
    data object Disable : RageshakeDetectionEvents
    data object StartDetection : RageshakeDetectionEvents
    data object StopDetection : RageshakeDetectionEvents
    data class ProcessScreenshot(val imageResult: ImageResult) : RageshakeDetectionEvents
}
