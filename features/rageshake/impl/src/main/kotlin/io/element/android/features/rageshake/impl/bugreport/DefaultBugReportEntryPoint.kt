/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultBugReportEntryPoint @Inject constructor() : BugReportEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): BugReportEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : BugReportEntryPoint.NodeBuilder {
            override fun callback(callback: BugReportEntryPoint.Callback): BugReportEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<BugReportNode>(buildContext, plugins)
            }
        }
    }
}
