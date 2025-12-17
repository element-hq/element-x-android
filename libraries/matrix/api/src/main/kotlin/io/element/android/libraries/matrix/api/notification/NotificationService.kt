/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Represents the resolution state of an attempt to retrieve notification data for a set of event ids.
 * The outer [Result] indicates the success or failure of the setup to retrieve notifications.
 * The inner [Result] for each [EventId] in the map indicates whether the notification data was successfully retrieved or if there was an error.
 */
typealias GetNotificationDataResult = Result<Map<EventId, Result<NotificationData>>>

/**
 * Service to retrieve notifications for a given set of event ids in specific rooms.
 */
interface NotificationService {
    /**
     * Fetch notifications for the specified event ids in the given rooms.
     */
    suspend fun getNotifications(ids: Map<RoomId, List<EventId>>): GetNotificationDataResult
}
