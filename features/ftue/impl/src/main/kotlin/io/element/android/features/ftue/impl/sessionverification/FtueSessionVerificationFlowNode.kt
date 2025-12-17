/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.features.ftue.impl.sessionverification.choosemode.ChooseSelfVerificationModeNode
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.designsystem.utils.OpenUrlInTabView
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class FtueSessionVerificationFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val outgoingVerificationEntryPoint: OutgoingVerificationEntryPoint,
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

    private val callback: Callback = callback()

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
                    override fun navigateToUseAnotherDevice() {
                        backstack.push(NavTarget.UseAnotherDevice)
                    }

                    override fun navigateToUseRecoveryKey() {
                        backstack.push(NavTarget.EnterRecoveryKey)
                    }

                    override fun navigateToResetKey() {
                        backstack.push(NavTarget.ResetIdentity)
                    }

                    override fun navigateToLearnMoreAboutEncryption() {
                        learnMoreUrl.value = LearnMoreConfig.DEVICE_VERIFICATION_URL
                    }
                }
                createNode<ChooseSelfVerificationModeNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.UseAnotherDevice -> {
                outgoingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = OutgoingVerificationEntryPoint.Params(
                        showDeviceVerifiedScreen = true,
                        verificationRequest = VerificationRequest.Outgoing.CurrentSession,
                    ),
                    callback = object : OutgoingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            callback.onDone()
                        }

                        override fun onBack() {
                            backstack.pop()
                        }

                        override fun navigateToLearnMoreAboutEncryption() {
                            // Note that this callback is never called. The "Learn more" link is not displayed
                            // for the self session interactive verification.
                        }
                    }
                )
            }
            is NavTarget.EnterRecoveryKey -> {
                secureBackupEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey),
                    callback = secureBackupEntryPointCallback
                )
            }
            is NavTarget.ResetIdentity -> {
                secureBackupEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.ResetIdentity),
                    callback = object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            callback.onDone()
                        }
                    },
                )
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
