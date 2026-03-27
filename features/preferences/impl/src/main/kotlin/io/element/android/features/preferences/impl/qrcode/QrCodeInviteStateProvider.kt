/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId

class QrCodeInviteStateProvider : PreviewParameterProvider<QrCodeInviteState>{
    override val values: Sequence<QrCodeInviteState>
        get() = sequenceOf(
            aQrCodeInviteState(loading = true, msk = null),
            aQrCodeInviteState(loading = false, msk = "SOME_RANDOM_DATA"),
            aQrCodeInviteState(loading = false, msk = null)
        )
}

fun aQrCodeInviteState(
    eventSink: (QrCodeInviteEvents) -> Unit = {},
    userId: String = "@alice:example.com",
    msk: String? = "SOME_RANDOM_DATA",
    loading: Boolean = true,
): QrCodeInviteState {
    return QrCodeInviteState(
        userId = UserId(userId),
        displayName = "John Doe",
        userAvatarUrl = null,
        loading = loading,
        qrCodeContent = msk?.let {"matrix:u/${userId.removePrefix("@")}?action=verify&msk=${Uri.encode(msk)}" },
        eventSink = eventSink,
    )
}
