/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.root.ViewFolderFlowNode
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
@Inject
class DefaultViewFolderEntryPoint : ViewFolderEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): ViewFolderEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : ViewFolderEntryPoint.NodeBuilder {
            override fun params(params: ViewFolderEntryPoint.Params): ViewFolderEntryPoint.NodeBuilder {
                plugins += ViewFolderFlowNode.Inputs(params.rootPath)
                return this
            }

            override fun callback(callback: ViewFolderEntryPoint.Callback): ViewFolderEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<ViewFolderFlowNode>(buildContext, plugins)
            }
        }
    }
}
