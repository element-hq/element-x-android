/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

sealed class NotificationResolverException : Exception() {
    data object EventNotFound : NotificationResolverException()
    data object EventFilteredOut : NotificationResolverException()
    data class UnknownError(override val message: String) : NotificationResolverException()
}
