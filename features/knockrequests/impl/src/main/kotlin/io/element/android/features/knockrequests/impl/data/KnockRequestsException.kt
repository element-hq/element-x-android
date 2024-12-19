/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

sealed class KnockRequestsException : Exception() {
    data object AcceptAllPartiallyFailed : KnockRequestsException()
    data object KnockRequestNotFound : KnockRequestsException()
}
