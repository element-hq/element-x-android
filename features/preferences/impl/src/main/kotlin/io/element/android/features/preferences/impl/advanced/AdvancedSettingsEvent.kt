/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

sealed interface AdvancedSettingsEvent {
    data class SetDeveloperModeEnabled(val enabled: Boolean) : AdvancedSettingsEvent
    data class SetSharePresenceEnabled(val enabled: Boolean) : AdvancedSettingsEvent
    data class SetCompressMedia(val compress: Boolean) : AdvancedSettingsEvent
    data class SetCompressImages(val compress: Boolean) : AdvancedSettingsEvent
    data class SetVideoUploadQuality(val videoPreset: VideoCompressionPreset) : AdvancedSettingsEvent
    data class SetTheme(val theme: ThemeOption) : AdvancedSettingsEvent
    data class SetTimelineMediaPreviewValue(val value: MediaPreviewValue) : AdvancedSettingsEvent
    data class SetHideInviteAvatars(val value: Boolean) : AdvancedSettingsEvent
    data class SetLiveLocationMinimumDistanceUpdate(val value: Int) : AdvancedSettingsEvent
}
