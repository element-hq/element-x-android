/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import io.element.android.features.location.api.Location
import io.element.android.features.location.api.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor() : LocationRepository {

    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    private val locationFlow: SharedFlow<Location> = _locationFlow.asSharedFlow()

    @Volatile
    private var lastKnownLocation: Location? = null

    override fun observeDistinct(): Flow<Location> = locationFlow.distinctUntilChanged()

    override fun send(location: Location) {
        lastKnownLocation = location
        _locationFlow.tryEmit(location)
    }

    override fun getLastLocationOrNull(): Location? = lastKnownLocation

    override suspend fun getLastLocationOrWait(): Location = locationFlow.first()
}
