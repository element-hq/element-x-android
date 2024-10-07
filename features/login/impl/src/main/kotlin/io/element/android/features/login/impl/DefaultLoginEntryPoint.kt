/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLoginEntryPoint @Inject constructor() : LoginEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): LoginEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : LoginEntryPoint.NodeBuilder {
            override fun params(params: LoginEntryPoint.Params): LoginEntryPoint.NodeBuilder {
                plugins += LoginFlowNode.Inputs(flowType = params.flowType)
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<LoginFlowNode>(buildContext, plugins)
            }
        }
    }
}
