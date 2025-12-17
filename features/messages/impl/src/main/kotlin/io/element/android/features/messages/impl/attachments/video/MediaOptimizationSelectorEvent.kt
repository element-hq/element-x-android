/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.video

import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

sealed interface MediaOptimizationSelectorEvent {
    data class SelectImageOptimization(val enabled: Boolean) : MediaOptimizationSelectorEvent
    data class SelectVideoPreset(val preset: VideoCompressionPreset) : MediaOptimizationSelectorEvent
    data object OpenVideoPresetSelectorDialog : MediaOptimizationSelectorEvent
    data object DismissVideoPresetSelectorDialog : MediaOptimizationSelectorEvent
}
