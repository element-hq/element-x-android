/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption.identity

import io.element.android.libraries.matrix.api.core.UserId

data class IdentityStateChange(
    val userId: UserId,
    val identityState: IdentityState,
)
