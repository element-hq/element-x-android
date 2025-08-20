/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultNotificationTroubleShootEntryPoint() : NotificationTroubleShootEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NotificationTroubleShootEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : NotificationTroubleShootEntryPoint.NodeBuilder {
            override fun callback(callback: NotificationTroubleShootEntryPoint.Callback): NotificationTroubleShootEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<TroubleshootNotificationsNode>(buildContext, plugins)
            }
        }
    }
}
