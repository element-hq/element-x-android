/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.features.ftue.impl.sessionverification.choosemode.ChooseSelfVerificationModeNode
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.designsystem.utils.OpenUrlInTabView
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class FtueSessionVerificationFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
) : BaseFlowNode<FtueSessionVerificationFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object UseAnotherDevice : NavTarget

        @Parcelize
        data object EnterRecoveryKey : NavTarget

        @Parcelize
        data object ResetIdentity : NavTarget
    }

    interface Callback : Plugin {
        fun onDone()
    }

    private val secureBackupEntryPointCallback = object : SecureBackupEntryPoint.Callback {
        override fun onDone() {
            lifecycleScope.launch {
                // Move to the completed state view in the verification flow
                backstack.newRoot(NavTarget.UseAnotherDevice)
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : ChooseSelfVerificationModeNode.Callback {
                    override fun onUseAnotherDevice() {
                        backstack.push(NavTarget.UseAnotherDevice)
                    }

                    override fun onUseRecoveryKey() {
                        backstack.push(NavTarget.EnterRecoveryKey)
                    }

                    override fun onResetKey() {
                        backstack.push(NavTarget.ResetIdentity)
                    }

                    override fun onLearnMoreAboutEncryption() {
                        learnMoreUrl.value = LearnMoreConfig.ENCRYPTION_URL
                    }
                }

                createNode<ChooseSelfVerificationModeNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.UseAnotherDevice -> {
                verifySessionEntryPoint.nodeBuilder(this, buildContext)
                    .params(VerifySessionEntryPoint.Params(
                        showDeviceVerifiedScreen = true,
                        verificationRequest = VerificationRequest.Outgoing.CurrentSession,
                    ))
                    .callback(object : VerifySessionEntryPoint.Callback {
                        override fun onDone() {
                            plugins<Callback>().forEach { it.onDone() }
                        }

                        override fun onBack() {
                            backstack.pop()
                        }

                        override fun onLearnMoreAboutEncryption() {
                            learnMoreUrl.value = LearnMoreConfig.ENCRYPTION_URL
                        }
                    })
                    .build()
            }
            is NavTarget.EnterRecoveryKey -> {
                secureBackupEntryPoint.nodeBuilder(this, buildContext)
                    .params(SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
                    .callback(secureBackupEntryPointCallback)
                    .build()
            }
            is NavTarget.ResetIdentity -> {
                secureBackupEntryPoint.nodeBuilder(this, buildContext)
                    .params(SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.ResetIdentity))
                    .callback(object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            plugins<Callback>().forEach { it.onDone() }
                        }
                    })
                    .build()
            }
        }
    }

    private val learnMoreUrl = mutableStateOf<String?>(null)

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()

        OpenUrlInTabView(learnMoreUrl)
    }
}
