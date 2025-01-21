/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.verifysession.api.IncomingVerificationEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultIncomingVerificationEntryPoint @Inject constructor() : IncomingVerificationEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): IncomingVerificationEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : IncomingVerificationEntryPoint.NodeBuilder {
            override fun callback(callback: IncomingVerificationEntryPoint.Callback): IncomingVerificationEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: IncomingVerificationEntryPoint.Params): IncomingVerificationEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<IncomingVerificationNode>(buildContext, plugins)
            }
        }
    }
}
