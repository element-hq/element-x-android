/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline

import androidx.compose.runtime.Composable
import io.element.android.wysiwyg.utils.HtmlConverter

/**
 * Provides the converter that turns message HTML into styled text.
 *
 * The converter depends on the current theme and typography, so [Update] must be composed before [provide] is called.
 */
interface HtmlConverterProvider {
    /** Rebuilds the converter from the current Compose theme; call this once, high in the composition of a screen that renders messages. */
    @Composable
    fun Update()

    /** Returns the converter built by the last [Update] call. */
    fun provide(): HtmlConverter
}
