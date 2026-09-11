/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.DelayedVisibility
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Ref: https://www.figma.com/design/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=1518-85323
 *
 * @param buildContext the [BuildContext] of the Node.
 * @param delayBeforeShowingContent when finite, a loading indicator fades in after this delay. Use it for
 * placeholders which can stay on screen for a long time, or forever, else the application looks frozen.
 * Defaults to [Duration.INFINITE], i.e. the Node stays empty.
 */
fun emptyNode(
    buildContext: BuildContext,
    delayBeforeShowingContent: Duration = Duration.INFINITE,
): Node = node(buildContext) { modifier ->
    EmptyView(
        delayBeforeShowingContent = delayBeforeShowingContent,
        modifier = modifier,
    )
}

@Composable
private fun EmptyView(
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
internal fun EmptyViewPreview() = ElementPreview {
    EmptyView(delayBeforeShowingContent = Duration.INFINITE)
}

@PreviewsDayNight
@Composable
internal fun EmptyViewLoadingPreview() = ElementPreview {
    EmptyView(delayBeforeShowingContent = 1.seconds)
}
