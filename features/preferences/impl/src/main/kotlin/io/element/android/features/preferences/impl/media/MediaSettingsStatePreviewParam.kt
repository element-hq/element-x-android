/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.media

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

open class MediaSettingsStatePreviewParam : PreviewParameterProvider<MediaSettingsState> {
    override val values: Sequence<MediaSettingsState>
        get() = sequenceOf(
            aMediaSettingsState(),
            aMediaSettingsState(mediaOptimizationState = MediaOptimizationState.AllMedia(isEnabled = true)),
            aMediaSettingsState(
                mediaOptimizationState = MediaOptimizationState.Split(
                    compressImages = true,
                    videoPreset = VideoCompressionPreset.HIGH,
                )
            ),
        )
}

fun aMediaSettingsState(
    mediaOptimizationState: MediaOptimizationState? = MediaOptimizationState.AllMedia(isEnabled = false),
    eventSink: (MediaSettingsEvent) -> Unit = {},
) = MediaSettingsState(
    mediaOptimizationState = mediaOptimizationState,
    eventSink = eventSink
)
