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

package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.MessagesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.nodeInputs
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.x.di.RoomComponent
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class RoomFlowNode(
    buildContext: BuildContext,
    plugins: List<Plugin>,
    private val backstack: BackStack<NavTarget>,
) : ParentNode<RoomFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext,
    plugins = plugins,
), DaggerComponentOwner {

    data class Inputs(
        val room: MatrixRoom,
    ) : NodeInputs

    @AssistedInject
    constructor(@Assisted buildContext: BuildContext, @Assisted plugins: List<Plugin>) : this(
        buildContext = buildContext,
        plugins = plugins,
        backstack = BackStack(
            initialElement = NavTarget.Messages,
            savedStateMap = buildContext.savedStateMap,
        ),
    )

    private val inputs: Inputs by nodeInputs()

    override val daggerComponent: Any by lazy {
        parent!!.bindings<RoomComponent.ParentBindings>().roomComponentBuilder().room(inputs.room).build()
    }

    init {
        lifecycle.subscribe(
            onCreate = { Timber.v("OnCreate") },
            onDestroy = { Timber.v("OnDestroy") }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Messages -> createNode<MessagesNode>(buildContext)
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
