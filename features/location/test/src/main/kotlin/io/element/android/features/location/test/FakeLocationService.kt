/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.test

import io.element.android.features.location.api.LocationService

class FakeLocationService(
    private val isServiceAvailable: Boolean = false,
) : LocationService {
    override fun isServiceAvailable() = isServiceAvailable
}
