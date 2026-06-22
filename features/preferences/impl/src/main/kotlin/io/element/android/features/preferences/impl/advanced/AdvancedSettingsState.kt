/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.preferences.api.store.TimelineLayoutMode
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val isSendReadReceiptsEnabled: Boolean,
    val isSendTypingNotificationsEnabled: Boolean,
    val isUrlPreviewEnabled: Boolean,
    val isDynamicColorEnabled: Boolean,
    val isHighContrastEnabled: Boolean,
    val mediaOptimizationState: MediaOptimizationState?,
    val theme: ThemeOption,
    val timelineLayoutMode: TimelineLayoutMode?,
    val availableThemeOptions: ImmutableList<ThemeOption> = ThemeOption.entries.toImmutableList(),
    val mediaPreviewConfigState: MediaPreviewConfigState,
    val liveLocationMinimumDistanceUpdate: Int? = null,
    val eventSink: (AdvancedSettingsEvents) -> Unit
)

sealed interface MediaOptimizationState {
    data class AllMedia(val isEnabled: Boolean) : MediaOptimizationState
    data class Split(
        val compressImages: Boolean,
        val videoPreset: VideoCompressionPreset,
    ) : MediaOptimizationState

    val shouldCompressImages: Boolean get() = when (this) {
        is AllMedia -> isEnabled
        is Split -> compressImages
    }
}

enum class TimelineLayoutOption : DropdownOption {
    Bubble {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.screen_advanced_settings_timeline_layout_bubble)
    },
    Modern {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.screen_advanced_settings_timeline_layout_modern)
    };

    fun toTimelineLayoutMode(): TimelineLayoutMode = when (this) {
        Bubble -> TimelineLayoutMode.Bubble
        Modern -> TimelineLayoutMode.Modern
    }

    companion object {
        fun from(mode: TimelineLayoutMode): TimelineLayoutOption = when (mode) {
            TimelineLayoutMode.Bubble -> Bubble
            TimelineLayoutMode.Modern -> Modern
        }
    }
}

enum class ThemeOption : DropdownOption {
    System {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_system)
    },

    Light {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_light)
    },

    Dark {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_dark)
    },

    Black {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_black)
    }
}
