/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.async

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ElementPaginationLoading

@Composable
fun AsyncLoading(modifier: Modifier = Modifier) {
    ElementPaginationLoading(modifier = modifier)
}

@PreviewsDayNight
@Composable
internal fun AsyncLoadingPreview() = ElementPreview {
    AsyncLoading()
}
