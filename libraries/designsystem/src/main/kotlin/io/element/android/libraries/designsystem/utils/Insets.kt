/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import io.element.android.libraries.designsystem.theme.components.Scaffold

/**
 * Content insets meant to be used for [Column] or [LazyColumn] components that are used as the main content of a screen,
 * to avoid overlapping with navigation bars, IME or cut-out display parts, but ignoring the status bar.
 *
 * Counterpart of [scaffoldScrollableContentInsets] for [Column] or [LazyColumn] components.
 */
val lazyColumnContentPadding @Composable
    get() = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        .asPaddingValues()

/**
 * Content insets meant to be used for [Scaffold] components that contain some content that is scrollable like [Column] or [LazyColumn],
 * and this content should handle its own content padding. The insets will only apply the top padding to avoid overlapping with the status bar,
 * while ignoring navigation bars, IME or cut-out display parts.
 *
 * Counterpart of [lazyColumnContentPadding] for [Scaffold] components.
 */
val scaffoldScrollableContentInsets @Composable
    get() = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top)
