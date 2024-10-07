/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.onboarding.api.OnBoardingEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultOnBoardingEntryPoint @Inject constructor() : OnBoardingEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): OnBoardingEntryPoint.NodeBuilder {
        return object : OnBoardingEntryPoint.NodeBuilder {
            val plugins = ArrayList<Plugin>()

            override fun callback(callback: OnBoardingEntryPoint.Callback): OnBoardingEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<OnBoardingNode>(buildContext, plugins)
            }
        }
    }
}
