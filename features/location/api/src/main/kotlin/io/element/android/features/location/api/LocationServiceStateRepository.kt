/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api

import kotlinx.coroutines.flow.StateFlow

interface LocationServiceStateRepository {
    fun observeDistinct(): StateFlow<LocationServiceState>

    fun set(state: LocationServiceState)
}
