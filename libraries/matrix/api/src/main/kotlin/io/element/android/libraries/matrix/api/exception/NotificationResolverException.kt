/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

/**
 * Exceptions that can occur while resolving the events associated to push notifications.
 */
sealed class NotificationResolverException : Exception() {
    /**
     * The event was not found by the notification service.
     */
    data object EventNotFound : NotificationResolverException()

    /**
     * The event was found but it was filtered out by the notification service.
     */
    data object EventFilteredOut : NotificationResolverException()

    /**
     * An unexpected error occurred while trying to resolve the event.
     */
    data class UnknownError(override val message: String) : NotificationResolverException()
}
