/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.replace
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.createroom.impl.addpeople.AddPeopleNode
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@Inject
class CreateRoomFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<CreateRoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.ConfigureRoom,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    private fun onRoomCreated(roomId: RoomId) {
        plugins<CreateRoomEntryPoint.Callback>().forEach { it.onRoomCreated(roomId) }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.ConfigureRoom -> {
                val callback = object : ConfigureRoomNode.Callback {
                    override fun onCreateRoomSuccess(roomId: RoomId) {
                        backstack.replace(NavTarget.AddPeople(roomId))
                    }
                }
                createNode<ConfigureRoomNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.AddPeople -> {
                val inputs = AddPeopleNode.Inputs(navTarget.roomId)
                val callback: AddPeopleNode.Callback = object : AddPeopleNode.Callback {
                    override fun onFinish() {
                        onRoomCreated(navTarget.roomId)
                    }
                }
                createNode<AddPeopleNode>(buildContext, plugins = listOf(inputs, callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object ConfigureRoom : NavTarget

        @Parcelize
        data class AddPeople(val roomId: RoomId) : NavTarget
    }
}
