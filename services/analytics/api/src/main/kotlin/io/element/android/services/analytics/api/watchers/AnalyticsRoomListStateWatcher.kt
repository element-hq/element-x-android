/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api.watchers

/**
 * This component is used to check how long it takes for the room list to be up to date after opening the app while it's on a 'warm' state:
 * the app was previously running and we just returned to it.
 */
interface AnalyticsRoomListStateWatcher {
    fun start()
    fun stop()
}
