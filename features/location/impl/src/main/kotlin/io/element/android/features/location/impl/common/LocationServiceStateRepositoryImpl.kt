/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import io.element.android.features.location.api.LocationServiceState
import io.element.android.features.location.api.LocationServiceStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocationServiceStateRepositoryImpl @Inject constructor(): LocationServiceStateRepository {

    private val _locationServiceStateFlow = MutableStateFlow(LocationServiceState.IDLE)
    private val locationServiceStateFlow: StateFlow<LocationServiceState> = _locationServiceStateFlow.asStateFlow()

    override fun observeDistinct(): StateFlow<LocationServiceState> = locationServiceStateFlow

    override fun set(state: LocationServiceState) {
        _locationServiceStateFlow.tryEmit(state)
    }
}
