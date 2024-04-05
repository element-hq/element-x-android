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

package io.element.android.features.securebackup.impl

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
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.securebackup.impl.disable.SecureBackupDisableNode
import io.element.android.features.securebackup.impl.enable.SecureBackupEnableNode
import io.element.android.features.securebackup.impl.enter.SecureBackupEnterRecoveryKeyNode
import io.element.android.features.securebackup.impl.root.SecureBackupRootNode
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class SecureBackupFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<SecureBackupFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = when (plugins.filterIsInstance(SecureBackupEntryPoint.Params::class.java).first().initialElement) {
            SecureBackupEntryPoint.InitialTarget.Root -> NavTarget.Root
            SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey -> NavTarget.EnterRecoveryKey
        },
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object Setup : NavTarget

        @Parcelize
        data object Change : NavTarget

        @Parcelize
        data object Disable : NavTarget

        @Parcelize
        data object Enable : NavTarget

        @Parcelize
        data object EnterRecoveryKey : NavTarget
    }

    private val callback = plugins<SecureBackupEntryPoint.Callback>().firstOrNull()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : SecureBackupRootNode.Callback {
                    override fun onSetupClicked() {
                        backstack.push(NavTarget.Setup)
                    }

                    override fun onChangeClicked() {
                        backstack.push(NavTarget.Change)
                    }

                    override fun onDisableClicked() {
                        backstack.push(NavTarget.Disable)
                    }

                    override fun onEnableClicked() {
                        backstack.push(NavTarget.Enable)
                    }

                    override fun onConfirmRecoveryKeyClicked() {
                        backstack.push(NavTarget.EnterRecoveryKey)
                    }
                }
                createNode<SecureBackupRootNode>(buildContext, listOf(callback))
            }
            NavTarget.Setup -> {
                val inputs = SecureBackupSetupNode.Inputs(
                    isChangeRecoveryKeyUserStory = false,
                )
                createNode<SecureBackupSetupNode>(buildContext, listOf(inputs))
            }
            NavTarget.Change -> {
                val inputs = SecureBackupSetupNode.Inputs(
                    isChangeRecoveryKeyUserStory = true,
                )
                createNode<SecureBackupSetupNode>(buildContext, listOf(inputs))
            }
            NavTarget.Disable -> {
                createNode<SecureBackupDisableNode>(buildContext)
            }
            NavTarget.Enable -> {
                createNode<SecureBackupEnableNode>(buildContext)
            }
            NavTarget.EnterRecoveryKey -> {
                val callback = object : SecureBackupEnterRecoveryKeyNode.Callback {
                    override fun onEnterRecoveryKeySuccess() {
                        if (callback != null) {
                            callback.onDone()
                        } else {
                            backstack.pop()
                        }
                    }
                }
                createNode<SecureBackupEnterRecoveryKeyNode>(buildContext, plugins = listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
