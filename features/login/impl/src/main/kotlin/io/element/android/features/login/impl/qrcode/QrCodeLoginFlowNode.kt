/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.qrcode

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.di.QrCodeLoginBindings
import io.element.android.features.login.impl.di.QrCodeLoginGraph
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationNode
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationStep
import io.element.android.features.login.impl.screens.qrcode.error.QrCodeErrorNode
import io.element.android.features.login.impl.screens.qrcode.intro.QrCodeIntroNode
import io.element.android.features.login.impl.screens.qrcode.scan.QrCodeScanNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(AppScope::class)
@AssistedInject
class QrCodeLoginFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    qrCodeLoginGraphFactory: QrCodeLoginGraph.Factory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BaseFlowNode<QrCodeLoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Initial,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DependencyInjectionGraphOwner {
    private var authenticationJob: Job? = null

    override val graph = qrCodeLoginGraphFactory.create()
    private val qrCodeLoginManager by lazy { bindings<QrCodeLoginBindings>().qrCodeLoginManager() }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Initial : NavTarget

        @Parcelize
        data object QrCodeScan : NavTarget

        @Parcelize
        data class QrCodeConfirmation(val step: QrCodeConfirmationStep) : NavTarget

        @Parcelize
        data class Error(val errorType: QrCodeErrorScreenType) : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()

        observeLoginStep()
    }

    fun isLoginInProgress(): Boolean {
        return authenticationJob?.isActive == true
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
                        is QrCodeLoginStep.Failed -> {
                            when (val error = step.error) {
                                is QrLoginException.OtherDeviceNotSignedIn -> {
                                    // Do nothing here, it'll be handled in the scan QR screen
                                }
                                is QrLoginException.Cancelled -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.Cancelled))
                                }
                                is QrLoginException.Expired -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.Expired))
                                }
                                is QrLoginException.Declined -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.Declined))
                                }
                                is QrLoginException.ConnectionInsecure -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.InsecureChannelDetected))
                                }
                                is QrLoginException.LinkingNotSupported -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.ProtocolNotSupported))
                                }
                                is QrLoginException.SlidingSyncNotAvailable -> {
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.SlidingSyncNotAvailable))
                                }
                                is QrLoginException.OidcMetadataInvalid -> {
                                    Timber.e(error, "OIDC metadata is invalid")
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.UnknownError))
                                }
                                else -> {
                                    Timber.e(error, "Unknown error found")
                                    backstack.replace(NavTarget.Error(QrCodeErrorScreenType.UnknownError))
                                }
                            }
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
                    override fun cancel() {
                        navigateUp()
                    }

                    override fun navigateToQrCodeScan() {
                        backstack.push(NavTarget.QrCodeScan)
                    }
                }
                createNode<QrCodeIntroNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.QrCodeScan -> {
                val callback = object : QrCodeScanNode.Callback {
                    override fun handleScannedCode(qrCodeLoginData: MatrixQrCodeLoginData) {
                        lifecycleScope.startAuthentication(qrCodeLoginData)
                    }

                    override fun cancel() {
                        backstack.pop()
                    }
                }
                createNode<QrCodeScanNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.QrCodeConfirmation -> {
                val callback = object : QrCodeConfirmationNode.Callback {
                    override fun onCancel() = reset()
                }
                createNode<QrCodeConfirmationNode>(buildContext, plugins = listOf(navTarget.step, callback))
            }
            is NavTarget.Error -> {
                val callback = object : QrCodeErrorNode.Callback {
                    override fun onRetry() = reset()
                }
                createNode<QrCodeErrorNode>(buildContext, plugins = listOf(navTarget.errorType, callback))
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun reset() {
        authenticationJob?.cancel()
        authenticationJob = null
        qrCodeLoginManager.reset()
        backstack.newRoot(NavTarget.Initial)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun CoroutineScope.startAuthentication(qrCodeLoginData: MatrixQrCodeLoginData) {
        authenticationJob = launch(coroutineDispatchers.main) {
            qrCodeLoginManager.authenticate(qrCodeLoginData)
                .onSuccess {
                    authenticationJob = null
                }
                .onFailure { throwable ->
                    Timber.e(throwable, "QR code authentication failed")
                    authenticationJob = null
                }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}

@Immutable
sealed interface QrCodeErrorScreenType : NodeInputs, Parcelable {
    @Parcelize
    data object Cancelled : QrCodeErrorScreenType

    @Parcelize
    data object Expired : QrCodeErrorScreenType

    @Parcelize
    data object InsecureChannelDetected : QrCodeErrorScreenType

    @Parcelize
    data object Declined : QrCodeErrorScreenType

    @Parcelize
    data object ProtocolNotSupported : QrCodeErrorScreenType

    @Parcelize
    data object SlidingSyncNotAvailable : QrCodeErrorScreenType

    @Parcelize
    data object UnknownError : QrCodeErrorScreenType
}
