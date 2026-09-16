/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

/**
 * A handle on an event sitting in a room's send queue, obtained from a timeline item that failed to send.
 */
fun interface SendHandle {
    /** Puts the event back in the send queue for another attempt. */
    suspend fun retry(): Result<Unit>
}
