/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api.watchers

/**
 * Adds a performance check transaction measuring the time between a cold start (or, after we read the user consent after a cold start)
 * until the cached room list is displayed. This check only takes place in a cold app start after the user is authenticated.
 */
interface AnalyticsColdStartWatcher {
    fun start()
    fun whenLoggingIn()
    fun onRoomListVisible()
}
