/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.media

import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

data class MediaSettingsState(
    val mediaOptimizationState: MediaOptimizationState?,
    val eventSink: (MediaSettingsEvent) -> Unit
)

sealed interface MediaOptimizationState {
    data class AllMedia(val isEnabled: Boolean) : MediaOptimizationState
    data class Split(
        val compressImages: Boolean,
        val videoPreset: VideoCompressionPreset,
    ) : MediaOptimizationState

    val shouldCompressImages: Boolean
        get() = when (this) {
            is AllMedia -> isEnabled
            is Split -> compressImages
        }
}
