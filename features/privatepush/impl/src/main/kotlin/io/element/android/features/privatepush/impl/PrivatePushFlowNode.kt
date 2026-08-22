/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.privatepush.api.PrivatePushEntryPoint
import io.element.android.features.privatepush.impl.setup.PrivatePushNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import kotlinx.parcelize.Parcelize

/** Setup flow + the upstream "Troubleshoot notifications" screen reachable from the Connect page. */
@ContributesNode(SessionScope::class)
@AssistedInject
class PrivatePushFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val notificationTroubleShootEntryPoint: NotificationTroubleShootEntryPoint,
) : BaseFlowNode<PrivatePushFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Setup,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Setup : NavTarget

        @Parcelize
        data object Troubleshoot : NavTarget
    }

    private val callback: PrivatePushEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Setup -> {
                val nodeCallback = object : PrivatePushNode.Callback {
                    override fun onDone() = callback.onDone()
                    override fun onLater() = callback.onLater()
                    override fun navigateToTroubleshoot() {
                        backstack.push(NavTarget.Troubleshoot)
                    }
                }
                createNode<PrivatePushNode>(buildContext, listOf(nodeCallback))
            }
            NavTarget.Troubleshoot -> {
                val troubleshootCallback = object : NotificationTroubleShootEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }

                    override fun navigateToBlockedUsers() = callback.navigateToBlockedUsers()
                }
                notificationTroubleShootEntryPoint.createNode(this, buildContext, troubleshootCallback)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
