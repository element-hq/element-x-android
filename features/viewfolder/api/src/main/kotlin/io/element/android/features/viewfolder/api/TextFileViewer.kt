/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

/**
 * Renders the contents of a text file, used by the log viewer in the developer options.
 */
fun interface TextFileViewer {
    /**
     * Draws the file, one row per line.
     *
     * @param lines the already-read contents of the file.
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        lines: ImmutableList<String>,
        modifier: Modifier,
    )
}
