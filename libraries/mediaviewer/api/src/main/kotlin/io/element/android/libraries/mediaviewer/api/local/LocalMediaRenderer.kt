/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import androidx.compose.runtime.Composable

/**
 * Renders a media file held on the device, picking the right player or viewer for its type.
 */
interface LocalMediaRenderer {
    /**
     * Draws the media, filling the space it is given.
     *
     * @param localMedia the file to display, along with what is known about its type.
     */
    @Composable
    fun Render(localMedia: LocalMedia)
}
