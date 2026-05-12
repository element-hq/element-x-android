/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

sealed interface MediaPreviewEvents {
    data object Send : MediaPreviewEvents
    data object Cancel : MediaPreviewEvents
    data object Retry : MediaPreviewEvents
    data object ClearError : MediaPreviewEvents
    data class ToggleImageOptimization(val enabled: Boolean) : MediaPreviewEvents
    data class SelectVideoQuality(val preset: VideoCompressionPreset) : MediaPreviewEvents
    data object ShowVideoQualityDialog : MediaPreviewEvents
    data object DismissVideoQualityDialog : MediaPreviewEvents
}
