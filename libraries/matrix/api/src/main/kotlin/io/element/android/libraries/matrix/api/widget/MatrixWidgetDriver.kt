/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import kotlinx.coroutines.flow.Flow

/**
 * Bridges a widget running in a WebView with the Matrix room it is embedded in, relaying the widget API messages in both directions.
 *
 * The driver owns SDK resources and must be closed once the widget goes away; Element Call is the main user of this.
 */
interface MatrixWidgetDriver : AutoCloseable {
    /** The id of the widget this driver serves, matching the one in its settings. */
    val id: String

    /** The messages coming from the room that should be forwarded to the widget. */
    val incomingMessages: Flow<String>

    /** Starts relaying messages and suspends for as long as the driver runs; calling it again while it is running does nothing. */
    suspend fun run()

    /**
     * Forwards a message from the widget to the room. Does nothing once the driver has been closed.
     *
     * @param message the raw widget API message emitted by the WebView.
     */
    suspend fun send(message: String)
}
