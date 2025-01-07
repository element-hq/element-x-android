/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultFtueEntryPoint @Inject constructor() : FtueEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): FtueEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : FtueEntryPoint.NodeBuilder {
            override fun build(): Node {
                return parentNode.createNode<FtueFlowNode>(buildContext, plugins)
            }
        }
    }
}
