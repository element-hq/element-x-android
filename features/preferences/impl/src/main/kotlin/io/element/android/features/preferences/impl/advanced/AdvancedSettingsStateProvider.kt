/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.media.MediaPreviewValue

open class AdvancedSettingsStateProvider : PreviewParameterProvider<AdvancedSettingsState> {
    override val values: Sequence<AdvancedSettingsState>
        get() = sequenceOf(
            aAdvancedSettingsState(),
            aAdvancedSettingsState(isDeveloperModeEnabled = true),
            aAdvancedSettingsState(isSharePresenceEnabled = true),
            aAdvancedSettingsState(doesCompressMedia = true),
            aAdvancedSettingsState(hideInviteAvatars = true),
            aAdvancedSettingsState(timelineMediaPreviewValue = MediaPreviewValue.Off)
        )
}

fun aAdvancedSettingsState(
    isDeveloperModeEnabled: Boolean = false,
    isSharePresenceEnabled: Boolean = false,
    doesCompressMedia: Boolean = false,
    hideInviteAvatars: Boolean = false,
    theme: ThemeOption = ThemeOption.System,
    timelineMediaPreviewValue: MediaPreviewValue = MediaPreviewValue.On,
    eventSink: (AdvancedSettingsEvents) -> Unit = {},
) = AdvancedSettingsState(
    isDeveloperModeEnabled = isDeveloperModeEnabled,
    isSharePresenceEnabled = isSharePresenceEnabled,
    doesCompressMedia = doesCompressMedia,
    theme = theme,
    hideInviteAvatars = hideInviteAvatars,
    timelineMediaPreviewValue = timelineMediaPreviewValue,
    eventSink = eventSink
)
