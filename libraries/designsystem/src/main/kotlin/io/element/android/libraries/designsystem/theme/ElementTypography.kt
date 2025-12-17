/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

// Temporary style for text that needs to be aligned without weird font padding issues.  `includeFontPadding` will default to false in a future version of
// compose, at which point this can be removed.
//
// Ref: https://medium.com/androiddevelopers/fixing-font-padding-in-compose-text-768cd232425b
@Suppress("DEPRECATION")
val noFontPadding: TextStyle = TextStyle(
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)
