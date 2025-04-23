/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import io.element.android.libraries.matrix.api.media.MediaPreviewValue

sealed interface AdvancedSettingsEvents {
    data class SetDeveloperModeEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data class SetSharePresenceEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data class SetCompressMedia(val compress: Boolean) : AdvancedSettingsEvents
    data class SetTheme(val theme: ThemeOption) : AdvancedSettingsEvents
    data class SetTimelineMediaPreviewValue(val value: MediaPreviewValue) : AdvancedSettingsEvents
    data class SetHideInviteAvatars(val value: Boolean) : AdvancedSettingsEvents
}
