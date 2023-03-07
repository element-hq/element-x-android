/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.nodeInputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class RoomFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val messagesEntryPoint: MessagesEntryPoint,
) : BackstackNode<RoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Messages,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    interface LifecycleCallback : NodeLifecycleCallback {
        fun onFlowCreated(room: MatrixRoom) = Unit
        fun onFlowReleased(room: MatrixRoom) = Unit
    }

    data class Inputs(
        val room: MatrixRoom,
    ) : NodeInputs

    private val inputs: Inputs by nodeInputs()

    init {
        lifecycle.subscribe(
            onCreate = {
                Timber.v("OnCreate")
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(inputs.room) }
            },
            onDestroy = {
                Timber.v("OnDestroy")
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(inputs.room) }
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Messages -> {
                messagesEntryPoint.node(this, buildContext)
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Messages : NavTarget
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
        )
    }
}
