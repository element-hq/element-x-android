/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.accountselect.api.AccountSelectEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultAccountSelectEntryPoint @Inject constructor() : AccountSelectEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): AccountSelectEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : AccountSelectEntryPoint.NodeBuilder {
            override fun callback(callback: AccountSelectEntryPoint.Callback): AccountSelectEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<AccountSelectNode>(buildContext, plugins)
            }
        }
    }
}
