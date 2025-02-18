/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import io.element.android.libraries.matrix.api.core.UserId

sealed interface IdentityChangeEvent {
    data class PinIdentity(val userId: UserId) : IdentityChangeEvent
    data class WithdrawVerification(val userId: UserId) : IdentityChangeEvent
}
