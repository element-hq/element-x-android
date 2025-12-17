/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.common.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Ref: https://www.figma.com/design/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=1518-85323
 */
fun emptyNode(
    buildContext: BuildContext,
): Node = node(buildContext) { modifier ->
    EmptyView(modifier)
}

@Composable
private fun EmptyView(
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .fillMaxSize()
        .background(ElementTheme.colors.bgCanvasDefault),
)

@PreviewsDayNight
@Composable
internal fun EmptyViewPreview() = ElementPreview {
    EmptyView(Modifier)
}
