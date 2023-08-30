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
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.ui.di.MatrixUIBindings
import kotlinx.parcelize.Parcelize

/**
 * `LoggedInAppScopeFlowNode` is a Node responsible to set up the Dagger
 * [io.element.android.libraries.di.SessionScope]. It has only one child: [LoggedInFlowNode].
 * This allow to inject objects with SessionScope in the constructor of [LoggedInFlowNode].
 */
@ContributesNode(AppScope::class)
class LoggedInAppScopeFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : ParentNode<LoggedInAppScopeFlowNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onOpenBugReport()
    }

    @Parcelize
    object NavTarget : Parcelable

    interface LifecycleCallback : NodeLifecycleCallback {
        fun onFlowCreated(identifier: String, client: MatrixClient)

        fun onFlowReleased(identifier: String, client: MatrixClient)
    }

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(id, inputs.matrixClient) }
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
            },
            onDestroy = {
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(id, inputs.matrixClient) }
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : LoggedInFlowNode.Callback {
            override fun onOpenBugReport() {
                plugins<Callback>().forEach { it.onOpenBugReport() }
            }
        }
        val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
        return createNode<LoggedInFlowNode>(buildContext, nodeLifecycleCallbacks + callback)
    }

    suspend fun attachSession(): LoggedInFlowNode {
        return waitForChildAttached { _ -> true }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = navModel,
            modifier = modifier,
        )
    }
}
