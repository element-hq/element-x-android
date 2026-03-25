/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * M3 Expressive loading indicator — replaces indeterminate [CircularProgressIndicator].
 *
 * Uses Material 3 Expressive [LoadingIndicator] with polygon shape morphing.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ElementLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    size: Dp = 48.dp,
) {
    if (LocalInspectionMode.current) {
        // Fallback to determinate circular for previews (LoadingIndicator animation won't render)
        CircularProgressIndicator(
            progress = { 0.75f },
            modifier = modifier.size(size),
        )
    } else {
        LoadingIndicator(
            modifier = modifier.size(size),
            color = color,
        )
    }
}

/**
 * Full-screen loading state using M3 Expressive [LoadingIndicator].
 */
@Composable
fun ElementFullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ElementLoadingIndicator()
    }
}

/**
 * Pagination / inline loading indicator, sized for list footers.
 */
@Composable
fun ElementPaginationLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        ElementLoadingIndicator(size = 32.dp)
    }
}

@PreviewsDayNight
@Composable
internal fun ElementLoadingIndicatorPreview() = ElementPreview {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ElementLoadingIndicator")
        ElementLoadingIndicator()
        Text("Small (32dp)")
        ElementLoadingIndicator(size = 32.dp)
    }
}

@PreviewsDayNight
@Composable
internal fun ElementPaginationLoadingPreview() = ElementPreview {
    ElementPaginationLoading()
}
