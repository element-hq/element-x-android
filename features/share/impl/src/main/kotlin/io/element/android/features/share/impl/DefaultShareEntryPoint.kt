/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultShareEntryPoint @Inject constructor() : ShareEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): ShareEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : ShareEntryPoint.NodeBuilder {
            override fun params(params: ShareEntryPoint.Params): ShareEntryPoint.NodeBuilder {
                plugins += ShareNode.Inputs(intent = params.intent)
                return this
            }

            override fun callback(callback: ShareEntryPoint.Callback): ShareEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<ShareNode>(buildContext, plugins)
            }
        }
    }
}
