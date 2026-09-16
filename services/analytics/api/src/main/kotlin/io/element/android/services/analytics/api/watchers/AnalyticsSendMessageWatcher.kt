/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api.watchers

/**
 * An analytics watcher tracking the time it took the client to send a message.
 */
interface AnalyticsSendMessageWatcher {
    /**
     * Start listening to send queue updates and tracking the sending states of the events.
     */
    fun start()

    /**
     * Stop observing the sending states of the events.
     */
    fun stop()
}
