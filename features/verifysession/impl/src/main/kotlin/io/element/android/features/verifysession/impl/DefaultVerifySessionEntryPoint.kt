/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultVerifySessionEntryPoint @Inject constructor() : VerifySessionEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): VerifySessionEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : VerifySessionEntryPoint.NodeBuilder {
            override fun callback(callback: VerifySessionEntryPoint.Callback): VerifySessionEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: VerifySessionEntryPoint.Params): VerifySessionEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<VerifySelfSessionNode>(buildContext, plugins)
            }
        }
    }
}
