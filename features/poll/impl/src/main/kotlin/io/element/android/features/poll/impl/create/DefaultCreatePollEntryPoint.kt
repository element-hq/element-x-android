/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCreatePollEntryPoint @Inject constructor() : CreatePollEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): CreatePollEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : CreatePollEntryPoint.NodeBuilder {
            override fun params(params: CreatePollEntryPoint.Params): CreatePollEntryPoint.NodeBuilder {
                plugins += CreatePollNode.Inputs(mode = params.mode)
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<CreatePollNode>(buildContext, plugins)
            }
        }
    }
}
