/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.securebackup.impl.disable.SecureBackupDisableNode
import io.element.android.features.securebackup.impl.enter.SecureBackupEnterRecoveryKeyNode
import io.element.android.features.securebackup.impl.reset.ResetIdentityFlowNode
import io.element.android.features.securebackup.impl.root.SecureBackupRootNode
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.canPop
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class SecureBackupFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<SecureBackupFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = when (plugins.filterIsInstance<SecureBackupEntryPoint.Params>().first().initialElement) {
            SecureBackupEntryPoint.InitialTarget.Root -> NavTarget.Root
            SecureBackupEntryPoint.InitialTarget.SetUpRecovery -> NavTarget.Setup
            SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey -> NavTarget.EnterRecoveryKey
            is SecureBackupEntryPoint.InitialTarget.ResetIdentity -> NavTarget.ResetIdentity
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
        data object EnterRecoveryKey : NavTarget

        @Parcelize
        data object ResetIdentity : NavTarget
    }

    private val callback: SecureBackupEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : SecureBackupRootNode.Callback {
                    override fun navigateToSetup() {
                        backstack.push(NavTarget.Setup)
                    }

                    override fun navigateToChange() {
                        backstack.push(NavTarget.Change)
                    }

                    override fun navigateToDisable() {
                        backstack.push(NavTarget.Disable)
                    }

                    override fun navigateToEnterRecoveryKey() {
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
            NavTarget.EnterRecoveryKey -> {
                val callback = object : SecureBackupEnterRecoveryKeyNode.Callback {
                    override fun onEnterRecoveryKeySuccess() {
                        if (backstack.canPop()) {
                            backstack.pop()
                        } else {
                            callback.onDone()
                        }
                    }
                }
                createNode<SecureBackupEnterRecoveryKeyNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.ResetIdentity -> {
                val callback = object : ResetIdentityFlowNode.Callback {
                    override fun onDone() {
                        callback.onDone()
                    }
                }
                createNode<ResetIdentityFlowNode>(buildContext, listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
