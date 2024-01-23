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

package io.element.android.features.createroom.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.createroom.impl.addpeople.AddPeopleNode
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomNode
import io.element.android.features.createroom.impl.di.CreateRoomComponent
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class ConfigureRoomFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : DaggerComponentOwner,
    BaseFlowNode<ConfigureRoomFlowNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.Root,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {
    private val component by lazy {
        parent!!.bindings<CreateRoomComponent.ParentBindings>().createRoomComponentBuilder().build()
    }

    override val daggerComponent: Any
        get() = component

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object ConfigureRoom : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : AddPeopleNode.Callback {
                    override fun onContinue() {
                        backstack.push(NavTarget.ConfigureRoom)
                    }
                }
                createNode<AddPeopleNode>(buildContext = buildContext, plugins = listOf(callback))
            }
            NavTarget.ConfigureRoom -> {
                val callbacks = plugins<ConfigureRoomNode.Callback>()
                createNode<ConfigureRoomNode>(buildContext = buildContext, plugins = callbacks)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
