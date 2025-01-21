/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPreferencesEntryPoint @Inject constructor() : PreferencesEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): PreferencesEntryPoint.NodeBuilder {
        return object : PreferencesEntryPoint.NodeBuilder {
            val plugins = ArrayList<Plugin>()

            override fun params(params: PreferencesEntryPoint.Params): PreferencesEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun callback(callback: PreferencesEntryPoint.Callback): PreferencesEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<PreferencesFlowNode>(buildContext, plugins)
            }
        }
    }
}

internal fun PreferencesEntryPoint.InitialTarget.toNavTarget() = when (this) {
    is PreferencesEntryPoint.InitialTarget.Root -> PreferencesFlowNode.NavTarget.Root
    is PreferencesEntryPoint.InitialTarget.NotificationSettings -> PreferencesFlowNode.NavTarget.NotificationSettings
    PreferencesEntryPoint.InitialTarget.NotificationTroubleshoot -> PreferencesFlowNode.NavTarget.TroubleshootNotifications
}
