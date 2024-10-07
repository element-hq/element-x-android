/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.compose.runtime.Composable

@Composable
fun ElementPreviewLight(
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        darkTheme = false,
        showBackground = showBackground,
        content = content
    )
}
