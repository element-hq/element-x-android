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

package io.element.android.features.lockscreen.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.lockscreen.impl.settings.LockScreenSettingsFlowNode
import io.element.android.features.lockscreen.impl.setup.LockScreenSetupFlowNode
import io.element.android.features.lockscreen.impl.unlock.PinUnlockNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class LockScreenFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<LockScreenFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance(Inputs::class.java).first().initialNavTarget,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    data class Inputs(
        val initialNavTarget: NavTarget = NavTarget.Unlock,
    ) : NodeInputs

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Unlock : NavTarget

        @Parcelize
        data object Setup : NavTarget

        @Parcelize
        data object Settings : NavTarget
    }

    private class OnSetupDoneCallback(private val plugins: List<LockScreenEntryPoint.Callback>) : LockScreenSetupFlowNode.Callback {
        override fun onSetupDone() {
            plugins.forEach {
                it.onSetupDone()
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Unlock -> {
                val inputs = PinUnlockNode.Inputs(isInAppUnlock = false)
                createNode<PinUnlockNode>(buildContext, plugins = listOf(inputs))
            }
            NavTarget.Setup -> {
                val callback = OnSetupDoneCallback(plugins())
                createNode<LockScreenSetupFlowNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.Settings -> {
                createNode<LockScreenSettingsFlowNode>(buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
