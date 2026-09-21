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
import io.element.android.features.preferences.impl.userstatus.UserStatusState
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class PreferencesRootState(
    val myUser: MatrixUser,
    val theme: ThemeOption,
    val availableThemeOptions: ImmutableList<ThemeOption>,
    val userStatusState: UserStatusState?,
    val version: String,
    val isMultiAccountEnabled: Boolean,
    val isOtherAccountsSectionExpanded: Boolean,
    val otherSessions: ImmutableList<MatrixUser>,
    val showAnalyticsSettings: Boolean,
    val showDeveloperSettings: Boolean,
    val showLabsItem: Boolean,
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
