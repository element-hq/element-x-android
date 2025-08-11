/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.attachments.video

import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

sealed interface MediaOptimizationSelectorEvent {
    data class SelectImageOptimization(val enabled: Boolean) : MediaOptimizationSelectorEvent
    data class SelectVideoPreset(val preset: VideoCompressionPreset) : MediaOptimizationSelectorEvent
}
