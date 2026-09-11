/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.common.nodes

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import kotlin.time.Duration

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
