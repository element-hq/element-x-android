/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.location

sealed class LiveLocationException(message: String?) : Exception(message) {
    class NotLive : LiveLocationException("The beacon event has expired.")
    class Network : LiveLocationException("Network error")
    class Other(val exception: Exception) : LiveLocationException(exception.message)
}
