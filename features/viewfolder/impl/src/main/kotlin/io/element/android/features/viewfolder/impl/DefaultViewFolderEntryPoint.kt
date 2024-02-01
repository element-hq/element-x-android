/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.viewfolder.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.root.ViewFolderRootNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultViewFolderEntryPoint @Inject constructor() : ViewFolderEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): ViewFolderEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : ViewFolderEntryPoint.NodeBuilder {
            override fun params(params: ViewFolderEntryPoint.Params): ViewFolderEntryPoint.NodeBuilder {
                plugins += ViewFolderRootNode.Inputs(params.rootPath)
                return this
            }

            override fun callback(callback: ViewFolderEntryPoint.Callback): ViewFolderEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<ViewFolderRootNode>(buildContext, plugins)
            }
        }
    }
}
