/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
