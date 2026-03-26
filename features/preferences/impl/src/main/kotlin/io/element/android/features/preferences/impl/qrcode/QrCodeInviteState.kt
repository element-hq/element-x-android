/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

import io.element.android.libraries.matrix.api.core.UserId

data class QrCodeInviteState(
    var userId: UserId,
    var displayName: String?,
    val userAvatarUrl: String?,
    val qrCodeContent: String,
    val eventSink: (QrCodeInviteEvents) -> Unit
)
