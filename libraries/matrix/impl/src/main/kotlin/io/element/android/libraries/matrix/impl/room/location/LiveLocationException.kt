/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.room.location.LiveLocationException
import org.matrix.rustcomponents.sdk.LiveLocationException as RustLiveLocationException

fun RustLiveLocationException.map(): LiveLocationException {
    return when (this) {
        is RustLiveLocationException.Network -> LiveLocationException.Network()
        is RustLiveLocationException.NotLive -> LiveLocationException.NotLive()
        else -> LiveLocationException.Other(this)
    }
}
