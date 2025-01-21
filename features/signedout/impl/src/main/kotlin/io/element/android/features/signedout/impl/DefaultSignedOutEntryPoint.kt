/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.signedout.api.SignedOutEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultSignedOutEntryPoint @Inject constructor() : SignedOutEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): SignedOutEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : SignedOutEntryPoint.NodeBuilder {
            override fun params(params: SignedOutEntryPoint.Params): SignedOutEntryPoint.NodeBuilder {
                plugins += SignedOutNode.Inputs(params.sessionId)
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<SignedOutNode>(buildContext, plugins)
            }
        }
    }
}
