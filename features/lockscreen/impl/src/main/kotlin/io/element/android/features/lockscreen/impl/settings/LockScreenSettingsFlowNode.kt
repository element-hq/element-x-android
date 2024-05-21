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

package io.element.android.features.lockscreen.impl.settings

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.setup.pin.SetupPinNode
import io.element.android.features.lockscreen.impl.unlock.PinUnlockNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class LockScreenSettingsFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pinCodeManager: PinCodeManager,
) : BaseFlowNode<LockScreenSettingsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Unknown,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Unknown : NavTarget

        @Parcelize
        data object Unlock : NavTarget

        @Parcelize
        data object SetupPin : NavTarget

        @Parcelize
        data object Settings : NavTarget
    }

    private val pinCodeManagerCallback = object : DefaultPinCodeManagerCallback() {
        override fun onPinCodeRemoved() {
            navigateUp()
        }

        override fun onPinCodeCreated() {
            backstack.newRoot(NavTarget.Settings)
        }
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            val hasPinCode = pinCodeManager.hasPinCode().first()
            if (hasPinCode) {
                backstack.newRoot(NavTarget.Unlock)
            } else {
                backstack.newRoot(NavTarget.SetupPin)
            }
        }
        lifecycle.subscribe(
            onCreate = {
                pinCodeManager.addCallback(pinCodeManagerCallback)
            },
            onDestroy = {
                pinCodeManager.removeCallback(pinCodeManagerCallback)
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Unlock -> {
                val callback = object : PinUnlockNode.Callback {
                    override fun onUnlock() {
                        backstack.newRoot(NavTarget.Settings)
                    }
                }
                createNode<PinUnlockNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.SetupPin -> {
                createNode<SetupPinNode>(buildContext)
            }
            NavTarget.Settings -> {
                val callback = object : LockScreenSettingsNode.Callback {
                    override fun onChangePinClicked() {
                        backstack.push(NavTarget.SetupPin)
                    }
                }
                createNode<LockScreenSettingsNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.Unknown -> node(buildContext) { }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
