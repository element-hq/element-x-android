/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.qrcode

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationNode
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationStep
import io.element.android.features.login.impl.screens.qrcode.error.QrCodeErrorNode
import io.element.android.features.login.impl.screens.qrcode.intro.QrCodeIntroNode
import io.element.android.features.login.impl.screens.qrcode.scan.QrCodeScanNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(AppScope::class)
class QrCodeLoginFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val qrCodeLoginManager: QrCodeLoginManager,
) : BaseFlowNode<QrCodeLoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Initial,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    private var authenticationJob: Job? = null

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Initial : NavTarget

        @Parcelize
        data object QrCodeScan : NavTarget

        @Parcelize
        data class QrCodeConfirmation(val step: QrCodeConfirmationStep) : NavTarget

        @Parcelize
        // TODO specify the error type
        data class Error(val message: String) : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()

        observeLoginStep()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun observeLoginStep() {
        lifecycleScope.launch {
            qrCodeLoginManager.currentLoginStep
                .collect { step ->
                    when (step) {
                        is QrCodeLoginStep.EstablishingSecureChannel -> {
                            backstack.replace(NavTarget.QrCodeConfirmation(QrCodeConfirmationStep.DisplayCheckCode(step.checkCode)))
                        }
                        is QrCodeLoginStep.WaitingForToken -> {
                            backstack.replace(NavTarget.QrCodeConfirmation(QrCodeConfirmationStep.DisplayVerificationCode(step.userCode)))
                        }
                        else -> Unit
                    }
                }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Initial -> {
                val callback = object : QrCodeIntroNode.Callback {
                    override fun onCancelClicked() {
                        navigateUp()
                    }

                    override fun onContinue() {
                        backstack.push(NavTarget.QrCodeScan)
                    }
                }
                createNode<QrCodeIntroNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.QrCodeScan -> {
                val callback = object : QrCodeScanNode.Callback {
                    override fun onScannedCode(qrCodeLoginData: MatrixQrCodeLoginData) {
                        lifecycleScope.startAuthentication(qrCodeLoginData)
                    }

                    override fun onCancelClicked() {
                        backstack.pop()
                    }
                }
                createNode<QrCodeScanNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.QrCodeConfirmation -> {
                val callback = object : QrCodeConfirmationNode.Callback {
                    override fun onCancel() {
                        authenticationJob?.cancel()
                        authenticationJob = null
                        backstack.newRoot(NavTarget.Initial)
                    }
                }
                createNode<QrCodeConfirmationNode>(buildContext, plugins = listOf(navTarget.step, callback))
            }
            is NavTarget.Error -> {
                // TODO specify the error type
                createNode<QrCodeErrorNode>(buildContext)
            }
        }
    }

    private fun CoroutineScope.startAuthentication(qrCodeLoginData: MatrixQrCodeLoginData) {
        authenticationJob = launch {
            qrCodeLoginManager.authenticate(qrCodeLoginData)
                .onSuccess {
                    println("Logged into session $it")
                    authenticationJob = null
                }
                .onFailure { throwable ->
                    // TODO specify the error type
                    Timber.e(throwable, "QR code authentication failed")
                    authenticationJob = null
                    if (throwable is CancellationException) {
                        throw throwable
                    }
                    backstack.push(NavTarget.Error(throwable.message ?: "Unknown error"))
                }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
