/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.root.ViewFolderFlowNode

@ContributesBinding(AppScope::class)
class DefaultViewFolderEntryPoint(
    private val viewFolderFlowNode: ViewFolderFlowNode,
) : ViewFolderEntryPoint {
    override fun createNode(
        buildContext: BuildContext,
        params: ViewFolderEntryPoint.Params,
        callback: ViewFolderEntryPoint.Callback,
    ): Node = node(buildContext) { modifier ->
        viewFolderFlowNode.View(
            rootPath = params.rootPath,
            onDone = callback::onDone,
            modifier = modifier,
        )
    }
}
