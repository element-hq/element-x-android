/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.service

import io.element.android.features.location.api.Location

fun interface LiveLocationReceiver {
    suspend fun onLocationUpdate(location: Location)
}
