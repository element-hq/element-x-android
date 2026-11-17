/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.account.PreferencesAccountState
import io.element.android.features.preferences.impl.userstatus.UserStatusState
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.switchaccount.OtherAccount
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

data class PreferencesRootState(
    val myUser: MatrixUser,
    val theme: ThemeOption,
    val timelineFontSize: TimelineFontSize,
    val preferencesAccountState: PreferencesAccountState,
    val availableThemeOptions: ImmutableList<ThemeOption>,
    val userStatusState: UserStatusState?,
    val version: String,
    val isMultiAccountEnabled: Boolean,
    val isOtherAccountsSectionExpanded: Boolean,
    val otherSessions: ImmutableList<OtherAccount>,
    val showAnalyticsSettings: Boolean,
    val showDeveloperSettings: Boolean,
    val showLabsItem: Boolean,
    val showLocationSettings: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (PreferencesRootEvent) -> Unit,
)

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

/** Sizes the timeline text can be set to, [Default] being the regular app size. */
enum class TimelineFontSize(val scale: Float) {
    Small(scale = 0.9f),
    Default(scale = 1f),
    Large(scale = 1.2f);
    @Composable
    @ReadOnlyComposable
    fun getText(): String = stringResource(
        when (this) {
            Small -> R.string.screen_settings_timeline_font_size_small
            Default -> R.string.screen_settings_timeline_font_size_default
            Large -> R.string.screen_settings_timeline_font_size_large
        }
    )

    companion object {
        /** Nearest stop to [scale], so an unknown stored value still lands on a valid one. */
        fun fromScale(scale: Float): TimelineFontSize = entries.minBy { kotlin.math.abs(it.scale - scale) }
    }
}

val TimelineFontSize.sliderValue: Float
    get() = ordinal / (TimelineFontSize.entries.size - 1f)

fun timelineFontSizeFromSliderValue(value: Float): TimelineFontSize {
    val index = (value * (TimelineFontSize.entries.size - 1)).roundToInt()
    return TimelineFontSize.entries[index.coerceIn(TimelineFontSize.entries.indices)]
}
