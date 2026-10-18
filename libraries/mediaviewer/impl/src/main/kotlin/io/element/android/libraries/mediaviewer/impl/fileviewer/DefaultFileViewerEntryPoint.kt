/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.mediaviewer.api.FileViewerEntryPoint

@ContributesBinding(AppScope::class)
class DefaultFileViewerEntryPoint : FileViewerEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: FileViewerEntryPoint.Params,
        callback: FileViewerEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<FileViewerNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback),
        )
    }
}
