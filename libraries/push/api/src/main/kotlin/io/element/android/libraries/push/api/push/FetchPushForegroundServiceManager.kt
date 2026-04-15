/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.push

/**
 * A helper to manage the foreground service used to keep the device awake while we schedule and wait for the work to fetch the notification content to run.
 */
interface FetchPushForegroundServiceManager {
    /**
     * Start the foreground service to acquire the wakelock. If the device is already awake, this method does nothing.
     *
     * @return true if the service was started, false otherwise (e.g. if the device was already awake or if starting the service failed).
     */
    fun start(): Boolean

    /**
     * Stop the foreground service to release the wakelock. If the service is not running, this method does nothing.
     *
     * @return true if the service was stopped, false otherwise (e.g. if the service was not running or if stopping the service failed).
     */
    suspend fun stop(): Boolean
}
