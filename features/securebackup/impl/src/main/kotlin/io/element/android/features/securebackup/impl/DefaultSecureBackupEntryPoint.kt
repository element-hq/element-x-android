/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultSecureBackupEntryPoint @Inject constructor() : SecureBackupEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): SecureBackupEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : SecureBackupEntryPoint.NodeBuilder {
            override fun params(params: SecureBackupEntryPoint.Params): SecureBackupEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun callback(callback: SecureBackupEntryPoint.Callback): SecureBackupEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<SecureBackupFlowNode>(buildContext, plugins)
            }
        }
    }
}
