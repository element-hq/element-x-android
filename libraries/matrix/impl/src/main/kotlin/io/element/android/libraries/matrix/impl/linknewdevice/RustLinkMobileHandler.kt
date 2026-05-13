/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgress
import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgressListener
import org.matrix.rustcomponents.sdk.GrantLoginWithQrCodeHandler
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException
import timber.log.Timber

private val tag = LoggerTag("RustLinkMobileHandler", LoggerTags.linkNewDevice)

class RustLinkMobileHandler(
    private val inner: GrantLoginWithQrCodeHandler,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
) : LinkMobileHandler {
    private val _linkMobileStep = MutableStateFlow<LinkMobileStep>(LinkMobileStep.Uninitialized)
    override val linkMobileStep: Flow<LinkMobileStep> = _linkMobileStep.asStateFlow()

    override suspend fun start() = withContext(sessionDispatcher) {
        Timber.tag(tag.value).d("Emit Uninitialized")
        _linkMobileStep.emit(LinkMobileStep.Uninitialized)
        try {
            inner.generate(
                progressListener = object : GrantGeneratedQrLoginProgressListener {
                    override fun onUpdate(state: GrantGeneratedQrLoginProgress) {
                        sessionCoroutineScope.launch {
                            val mappedState = state.map()
                            Timber.tag(tag.value).d("Emit ${mappedState::class.java.simpleName}")
                            _linkMobileStep.emit(mappedState)
                        }
                    }
                }
            )
            // We emit Done in case the progress listener was deallocated before generate() sent the Done
            _linkMobileStep.emit(LinkMobileStep.Done)
        } catch (e: HumanQrGrantLoginException) {
            Timber.tag(tag.value).w(e, "Error during QR login grant")
            _linkMobileStep.emit(LinkMobileStep.Error(e.map()))
        }
    }

    private fun GrantGeneratedQrLoginProgress.map(): LinkMobileStep {
        return when (this) {
            GrantGeneratedQrLoginProgress.Done -> LinkMobileStep.Done
            is GrantGeneratedQrLoginProgress.QrReady -> {
                LinkMobileStep.QrReady(String(qrCode.toBytes(), Charsets.ISO_8859_1))
            }
            is GrantGeneratedQrLoginProgress.QrScanned -> LinkMobileStep.QrScanned(
                RustCheckCodeSender(
                    inner = checkCodeSender,
                    sessionDispatcher = sessionDispatcher,
                )
            )
            GrantGeneratedQrLoginProgress.Starting -> LinkMobileStep.Starting
            GrantGeneratedQrLoginProgress.SyncingSecrets -> LinkMobileStep.SyncingSecrets
            is GrantGeneratedQrLoginProgress.WaitingForAuth -> LinkMobileStep.WaitingForAuth(verificationUri)
        }
    }
}
