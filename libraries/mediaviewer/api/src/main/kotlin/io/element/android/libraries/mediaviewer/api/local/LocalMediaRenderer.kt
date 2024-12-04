/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import androidx.compose.runtime.Composable

interface LocalMediaRenderer {
    @Composable
    fun Render(localMedia: LocalMedia)
}
