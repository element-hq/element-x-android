/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.common.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.DelayedVisibility
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.time.Duration

@Composable
fun EmptyView(
    delayBeforeShowingContent: Duration,
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .fillMaxSize()
        .background(ElementTheme.colors.bgCanvasDefault),
    contentAlignment = Alignment.Center,
) {
    if (delayBeforeShowingContent.isFinite()) {
        DelayedVisibility(duration = delayBeforeShowingContent) {
            LoadingContent()
        }
    }
}

@Composable
private fun LoadingContent() = Column(
    modifier = Modifier.padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    CircularProgressIndicator()
    Text(
        text = stringResource(CommonStrings.common_loading),
        color = ElementTheme.colors.textSecondary,
        style = ElementTheme.typography.fontBodyMdRegular,
    )
}

@PreviewsDayNight
@Composable
internal fun EmptyViewPreview(
    @PreviewParameter(DurationPreviewParam::class) duration: Duration
) = ElementPreview {
    EmptyView(delayBeforeShowingContent = duration)
}
