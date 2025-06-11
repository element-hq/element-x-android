/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val doesCompressMedia: Boolean,
    val theme: ThemeOption,
    val hideInviteAvatars: Boolean,
    val timelineMediaPreviewValue: MediaPreviewValue,
    val eventSink: (AdvancedSettingsEvents) -> Unit
)

enum class ThemeOption : DropdownOption {
    System {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(CommonStrings.common_system)
    },
    Dark {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(CommonStrings.common_dark)
    },
    Light {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(CommonStrings.common_light)
    }
}
