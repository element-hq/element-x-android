/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.startchat.DefaultStartChatNavigator
import io.element.android.features.startchat.api.StartChatEntryPoint
import io.element.android.features.startchat.impl.joinbyaddress.JoinRoomByAddressNode
import io.element.android.features.startchat.impl.root.StartChatNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.OverlayView
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class StartChatFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<StartChatFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object NewRoom : NavTarget

        @Parcelize
        data object JoinByAddress : NavTarget
    }

    private val navigator = DefaultStartChatNavigator(
        backstack = backstack,
        overlay = overlay,
        openRoom = { roomIdOrAlias, viaServers ->
            plugins<StartChatEntryPoint.Callback>().forEach { it.onOpenRoom(roomIdOrAlias, viaServers) }
        },
        openRoomDirectory = {
            plugins<StartChatEntryPoint.Callback>().forEach { it.onOpenRoomDirectory() }
        }
    )

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                createNode<StartChatNode>(buildContext = buildContext, plugins = listOf(navigator))
            }
            NavTarget.NewRoom -> {
                createNode<CreateRoomFlowNode>(buildContext = buildContext, plugins = listOf(navigator))
            }
            NavTarget.JoinByAddress -> {
                createNode<JoinRoomByAddressNode>(buildContext = buildContext, plugins = listOf(navigator))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            BackstackView()
            OverlayView(transitionHandler = remember { JumpToEndTransitionHandler() })
        }
    }
}
