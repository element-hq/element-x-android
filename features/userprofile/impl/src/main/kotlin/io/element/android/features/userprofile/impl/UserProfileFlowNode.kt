/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.features.userprofile.impl.root.UserProfileNode
import io.element.android.features.userprofile.shared.UserProfileNodeHelper
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class UserProfileFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val sessionIdHolder: CurrentSessionIdHolder,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
) : BaseFlowNode<UserProfileFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : UserProfileNodeHelper.Callback {
                    override fun openAvatarPreview(username: String, avatarUrl: String) {
                        backstack.push(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun onStartDM(roomId: RoomId) {
                        plugins<UserProfileEntryPoint.Callback>().forEach { it.onOpenRoom(roomId) }
                    }

                    override fun onStartCall(dmRoomId: RoomId) {
                        elementCallEntryPoint.startCall(CallType.RoomCall(sessionId = sessionIdHolder.current, roomId = dmRoomId))
                    }
                }
                val params = UserProfileNode.UserProfileInputs(userId = inputs<UserProfileEntryPoint.Params>().userId)
                createNode<UserProfileNode>(buildContext, listOf(callback, params))
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                       // Cannot happen
                    }
                }
                mediaViewerEntryPoint.nodeBuilder(this, buildContext)
                    .avatar(
                        filename = navTarget.name,
                        avatarUrl = navTarget.avatarUrl
                    )
                    .callback(callback)
                    .build()
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
