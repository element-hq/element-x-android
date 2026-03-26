/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.matrix.api.MatrixClient

@Inject
class QrCodeInvitePresenter(
    private val matrixClient: MatrixClient,

):  Presenter<QrCodeInviteState> {


    @Composable
    override fun present(): QrCodeInviteState {
        val matrixUser = matrixClient.userProfile.collectAsState()

        fun handleEvent(event: QrCodeInviteEvents) {
            when (event) {
                QrCodeInviteEvents.ScanQrCode -> {}
            }
        }

        val userId = matrixUser.value.userId
        val qrCodeContent = "matrix:u/${userId.value.substring(1)}"

        return QrCodeInviteState(
            userId,
            displayName = matrixUser.value.displayName,
            userAvatarUrl = matrixUser.value.avatarUrl,
            qrCodeContent,
            eventSink = ::handleEvent,
        )
    }
}
