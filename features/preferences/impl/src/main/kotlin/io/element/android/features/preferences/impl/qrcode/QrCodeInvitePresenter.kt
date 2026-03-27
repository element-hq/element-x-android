/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
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

        val mskState by produceState<MskState>(initialValue = MskState.Loading) {
            val identity = matrixClient
                .encryptionService
                .getUserIdentity(userId, true)
                .getOrNull()

            value = MskState.Loaded(identity?.msk)
        }

        val qrCodeContent = remember(mskState, userId) {
            mskState.valueOrNull?.let {
                val localUserId = userId.value.removePrefix("@")
                val msk = Uri.encode(it)
                "matrix:u/$localUserId?action=verify&msk=$msk"
            }
        }

        return QrCodeInviteState(
            userId = matrixUser.value.userId,
            displayName = matrixUser.value.displayName,
            userAvatarUrl = matrixUser.value.avatarUrl,
            loading = mskState.isLoading,
            qrCodeContent = qrCodeContent,
            eventSink = ::handleEvent,
        )
    }
}
