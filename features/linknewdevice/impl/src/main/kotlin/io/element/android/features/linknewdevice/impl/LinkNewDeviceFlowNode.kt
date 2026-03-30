/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.linknewdevice.api.LinkNewDeviceEntryPoint
import io.element.android.features.linknewdevice.impl.screens.desktop.DesktopNoticeNode
import io.element.android.features.linknewdevice.impl.screens.error.ErrorNode
import io.element.android.features.linknewdevice.impl.screens.error.ErrorScreenType
import io.element.android.features.linknewdevice.impl.screens.number.EnterNumberNode
import io.element.android.features.linknewdevice.impl.screens.qrcode.ShowQrCodeNode
import io.element.android.features.linknewdevice.impl.screens.root.LinkNewDeviceRootNode
import io.element.android.features.linknewdevice.impl.screens.scan.ScanQrCodeNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopStep
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber

private val tag = LoggerTag("LinkNewDeviceFlowNode", LoggerTags.linkNewDevice)

@ContributesNode(SessionScope::class)
@AssistedInject
class LinkNewDeviceFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val linkNewMobileHandler: LinkNewMobileHandler,
    private val linkNewDesktopHandler: LinkNewDesktopHandler,
) : BaseFlowNode<LinkNewDeviceFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    private val callback: LinkNewDeviceEntryPoint.Callback = callback()
    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    override fun onBuilt() {
        super.onBuilt()
        var linkMobileHandlerJob: Job? = null
        var linkDesktopHandlerJob: Job? = null

        lifecycle.subscribe(
            onCreate = {
                linkNewMobileHandler.reset()
                linkNewDesktopHandler.reset()
                @Suppress("AssignedValueIsNeverRead")
                linkMobileHandlerJob = observeLinkNewMobileHandler()
                @Suppress("AssignedValueIsNeverRead")
                linkDesktopHandlerJob = observeLinkNewDesktopHandler()
            },
            onDestroy = {
                linkMobileHandlerJob?.cancel()
                linkDesktopHandlerJob?.cancel()
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        // Will display the not supported state or the device type selection
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class MobileShowQrCode(
            val data: String,
        ) : NavTarget

        @Parcelize
        data object MobileEnterNumber : NavTarget

        @Parcelize
        data object DesktopNotice : NavTarget

        @Parcelize
        data object DesktopScanQrCode : NavTarget

        @Parcelize
        data class Error(
            val errorScreenType: ErrorScreenType,
        ) : NavTarget
    }

    private fun observeLinkNewMobileHandler(): Job {
        Timber.tag(tag.value).d("startObservingLinkNewMobileHandler")
        return linkNewMobileHandler.stepFlow
            .onEach { linkMobileStep ->
                Timber.tag(tag.value).d("step: ${linkMobileStep::class.java.simpleName}")
                when (linkMobileStep) {
                    LinkMobileStep.Uninitialized -> Unit
                    LinkMobileStep.Done -> {
                        callback.onDone()
                    }
                    is LinkMobileStep.Error -> {
                        navigateToError(linkMobileStep.errorType)
                    }
                    is LinkMobileStep.QrReady -> {
                        // The QrCode is ready, navigate to its display
                        backstack.push(NavTarget.MobileShowQrCode(linkMobileStep.data))
                    }
                    is LinkMobileStep.QrScanned -> {
                        backstack.replace(NavTarget.MobileEnterNumber)
                    }
                    LinkMobileStep.Starting -> {
                        // This step is not received at the moment, so do nothing
                    }
                    LinkMobileStep.SyncingSecrets -> {
                        // LinkMobileStep.Done is not received at the moment, so consider that the flow is done here
                        callback.onDone()
                    }
                    is LinkMobileStep.WaitingForAuth -> {
                        navigateToBrowser(linkMobileStep.verificationUri)
                    }
                }
            }
            .launchIn(sessionCoroutineScope)
    }

    private fun observeLinkNewDesktopHandler(): Job {
        Timber.tag(tag.value).d("startObservingLinkNewDesktopHandler")
        return linkNewDesktopHandler.stepFlow.onEach { linkDesktopStep ->
            Timber.tag(tag.value).d("step: ${linkDesktopStep::class.java.simpleName}")
            when (linkDesktopStep) {
                LinkDesktopStep.Done -> callback.onDone()
                is LinkDesktopStep.Error -> {
                    navigateToError(linkDesktopStep.errorType)
                }
                is LinkDesktopStep.EstablishingSecureChannel -> Unit
                is LinkDesktopStep.InvalidQrCode -> {
                    // This error will be handled by the ScanQrCodeNode
                }
                LinkDesktopStep.Starting -> Unit
                LinkDesktopStep.SyncingSecrets -> Unit
                LinkDesktopStep.Uninitialized -> Unit
                is LinkDesktopStep.WaitingForAuth -> {
                    navigateToBrowser(linkDesktopStep.verificationUri)
                }
            }
        }
            .launchIn(sessionCoroutineScope)
    }

    private fun navigateToError(errorType: ErrorType) {
        // Map the error to an error screen
        // TODO Update this mapping
        val error = when (errorType) {
            is ErrorType.DeviceIdAlreadyInUse -> ErrorScreenType.UnknownError
            is ErrorType.InvalidCheckCode -> ErrorScreenType.InsecureChannelDetected
            is ErrorType.MissingSecretsBackup -> ErrorScreenType.UnknownError
            is ErrorType.NotFound -> ErrorScreenType.Expired
            is ErrorType.DeviceNotFound -> ErrorScreenType.UnknownError
            is ErrorType.Unknown -> ErrorScreenType.UnknownError
            is ErrorType.UnsupportedProtocol -> ErrorScreenType.UnknownError
            is ErrorType.Cancelled -> ErrorScreenType.UnknownError
            is ErrorType.ConnectionInsecure -> ErrorScreenType.InsecureChannelDetected
            is ErrorType.Expired -> ErrorScreenType.Expired
            is ErrorType.OtherDeviceAlreadySignedIn -> ErrorScreenType.UnknownError
            is ErrorType.UnsupportedQrCodeType -> ErrorScreenType.UnknownError
        }
        // It is OK to push on backstack, since when user leaves the error screen, a new root will be set,
        // or the whole flow will be popped.
        backstack.push(NavTarget.Error(error))
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : LinkNewDeviceRootNode.Callback {
                    override fun onDone() {
                        callback.onDone()
                    }

                    override fun linkDesktopDevice() {
                        linkNewDesktopHandler.reset()
                        backstack.push(NavTarget.DesktopNotice)
                    }
                }
                createNode<LinkNewDeviceRootNode>(buildContext, listOf(callback))
            }
            NavTarget.DesktopNotice -> {
                val callback = object : DesktopNoticeNode.Callback {
                    override fun navigateBack() {
                        backstack.pop()
                    }

                    override fun navigateToQrCodeScanner() {
                        backstack.push(NavTarget.DesktopScanQrCode)
                    }
                }
                createNode<DesktopNoticeNode>(buildContext, listOf(callback))
            }
            NavTarget.DesktopScanQrCode -> {
                val callback = object : ScanQrCodeNode.Callback {
                    override fun cancel() {
                        backstack.pop()
                    }
                }
                createNode<ScanQrCodeNode>(buildContext, listOf(callback))
            }
            NavTarget.MobileEnterNumber -> {
                val callback = object : EnterNumberNode.Callback {
                    override fun navigateToWrongNumberError() {
                        backstack.push(NavTarget.Error(ErrorScreenType.Mismatch2Digits))
                    }

                    override fun navigateBack() {
                        backstack.pop()
                    }
                }
                createNode<EnterNumberNode>(buildContext, listOf(callback))
            }
            is NavTarget.MobileShowQrCode -> {
                val callback = object : ShowQrCodeNode.Callback {
                    override fun navigateBack() {
                        linkNewMobileHandler.reset()
                        backstack.pop()
                    }
                }
                val inputs = ShowQrCodeNode.Inputs(
                    data = navTarget.data,
                )
                createNode<ShowQrCodeNode>(buildContext, listOf(inputs, callback))
            }
            is NavTarget.Error -> {
                val callback = object : ErrorNode.Callback {
                    override fun onRetry() {
                        linkNewMobileHandler.reset()
                        linkNewDesktopHandler.reset()
                        backstack.newRoot(NavTarget.Root)
                    }

                    override fun onCancel() {
                        linkNewMobileHandler.reset()
                        linkNewDesktopHandler.reset()
                        callback.onDone()
                    }
                }
                createNode<ErrorNode>(buildContext, listOf(callback, navTarget.errorScreenType))
            }
        }
    }

    private fun navigateToBrowser(url: String) {
        activity?.openUrlInChromeCustomTab(null, darkTheme, url)
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = requireNotNull(LocalActivity.current)
        darkTheme = !ElementTheme.isLightTheme
        DisposableEffect(Unit) {
            onDispose {
                activity = null
            }
        }
        BackstackView()
    }
}
