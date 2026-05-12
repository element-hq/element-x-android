/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import io.element.android.libraries.matrix.impl.auth.qrlogin.QrErrorMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.GrantLoginWithQrCodeHandler
import org.matrix.rustcomponents.sdk.GrantQrLoginProgress
import org.matrix.rustcomponents.sdk.GrantQrLoginProgressListener
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException
import org.matrix.rustcomponents.sdk.QrCodeDecodeException
import timber.log.Timber

private val tag = LoggerTag("RustLinkDesktopHandler", LoggerTags.linkNewDevice)

class RustLinkDesktopHandler(
    private val inner: GrantLoginWithQrCodeHandler,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
    private val qrCodeDataParser: QrCodeDataParser,
) : LinkDesktopHandler {
    private val _linkDesktopStep = MutableStateFlow<LinkDesktopStep>(LinkDesktopStep.Uninitialized)
    override val linkDesktopStep: StateFlow<LinkDesktopStep> = _linkDesktopStep.asStateFlow()

    override suspend fun handleScannedQrCode(data: ByteArray) = withContext(sessionDispatcher) {
        Timber.tag(tag.value).d("Emit Uninitialized")
        _linkDesktopStep.emit(LinkDesktopStep.Uninitialized)
        try {
            val qrCodeData = qrCodeDataParser.parse(data)
            inner.scan(
                qrCodeData = qrCodeData,
                progressListener = object : GrantQrLoginProgressListener {
                    override fun onUpdate(state: GrantQrLoginProgress) {
                        sessionCoroutineScope.launch {
                            val mappedState = state.map()
                            Timber.tag(tag.value).d("Emit ${mappedState::class.java.simpleName}")
                            _linkDesktopStep.emit(mappedState)
                        }
                    }
                }
            )
            // We emit Done in case the progress listener was deallocated before scan() sent the Done
            _linkDesktopStep.emit(LinkDesktopStep.Done)
        } catch (e: QrCodeDecodeException) {
            Timber.tag(tag.value).w(e, "Invalid QR code scanned")
            _linkDesktopStep.emit(
                LinkDesktopStep.InvalidQrCode(
                    error = QrErrorMapper.map(e)
                )
            )
        } catch (e: HumanQrGrantLoginException) {
            Timber.tag(tag.value).w(e, "Error during QR login grant")
            _linkDesktopStep.emit(LinkDesktopStep.Error(e.map()))
        }
    }

    private fun GrantQrLoginProgress.map() = when (this) {
        GrantQrLoginProgress.Done -> LinkDesktopStep.Done
        GrantQrLoginProgress.Starting -> LinkDesktopStep.Starting
        GrantQrLoginProgress.SyncingSecrets -> LinkDesktopStep.SyncingSecrets
        is GrantQrLoginProgress.WaitingForAuth -> LinkDesktopStep.WaitingForAuth(
            verificationUri = verificationUri,
        )
        is GrantQrLoginProgress.EstablishingSecureChannel -> LinkDesktopStep.EstablishingSecureChannel(
            checkCode = checkCode,
            checkCodeString = checkCodeString,
        )
    }
}
