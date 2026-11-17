/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.securebackup.api.SecureBackupRecoveryKeyEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

/**
 * The flow of removing the device: a question about it, which can take the user through their recovery key first.
 */
@ContributesNode(SessionScope::class)
@AssistedInject
class LogoutFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val recoveryKeyEntryPoint: SecureBackupRecoveryKeyEntryPoint,
) : BaseFlowNode<LogoutFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root(removeOnceKeysAreBackedUp = false),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data class Root(val removeOnceKeysAreBackedUp: Boolean) : NavTarget

        @Parcelize
        data class GenerateRecoveryKey(val isReplacingExistingKey: Boolean) : NavTarget

        @Parcelize
        data object CheckRecoveryKey : NavTarget
    }

    private val callback: LogoutEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val rootCallback = object : LogoutNode.Callback {
                    override fun onDismiss() = callback.onDismiss()

                    override fun onGenerateRecoveryKey(isReplacingExistingKey: Boolean) {
                        backstack.push(NavTarget.GenerateRecoveryKey(isReplacingExistingKey))
                    }

                    override fun onCheckRecoveryKey() {
                        backstack.push(NavTarget.CheckRecoveryKey)
                    }

                    override fun onOpenSecureBackupSettings() {
                        callback.onDismiss()
                        callback.navigateToSecureBackup()
                    }
                }
                createNode<LogoutNode>(
                    buildContext = buildContext,
                    plugins = listOf(LogoutNode.Inputs(navTarget.removeOnceKeysAreBackedUp), rootCallback),
                )
            }
            is NavTarget.GenerateRecoveryKey -> recoveryKeyNode(
                buildContext = buildContext,
                mode = SecureBackupRecoveryKeyEntryPoint.Mode.Generate(isReplacingExistingKey = navTarget.isReplacingExistingKey),
            )
            NavTarget.CheckRecoveryKey -> recoveryKeyNode(
                buildContext = buildContext,
                mode = SecureBackupRecoveryKeyEntryPoint.Mode.Check,
            )
        }
    }

    private fun recoveryKeyNode(buildContext: BuildContext, mode: SecureBackupRecoveryKeyEntryPoint.Mode): Node {
        return recoveryKeyEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            inputs = SecureBackupRecoveryKeyEntryPoint.Inputs(
                mode = mode,
                doneButtonText = R.string.screen_signout_save_recovery_key_action_continue,
            ),
            callback = object : SecureBackupRecoveryKeyEntryPoint.Callback {
                override fun onDone() {
                    // The user is done with their key, so the removal carries on instead of asking again.
                    backstack.newRoot(NavTarget.Root(removeOnceKeysAreBackedUp = true))
                }
            },
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier)
    }
}
