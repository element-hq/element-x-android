/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId

class QrCodeInviteStateProvider : PreviewParameterProvider<QrCodeInviteState>{
    override val values: Sequence<QrCodeInviteState>
        get() = sequenceOf(aQrCodeInviteState())
}

fun aQrCodeInviteState(
    eventSink: (QrCodeInviteEvents) -> Unit = {},
): QrCodeInviteState {
    return QrCodeInviteState(
        userId = UserId("@john.doe:matrix.org"),
        displayName = "John Doe",
        userAvatarUrl = null,
        qrCodeContent = "matrix:u/john.doe:matrix.org",
        eventSink = eventSink,
    )
}
