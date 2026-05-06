/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.location

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.NullLocationProvider
import org.maplibre.compose.location.PermissionException
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidDeviceLocationProvider(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    appPreferencesStore: AppPreferencesStore,
) : DeviceLocationProvider {
    private val refreshFlow = MutableSharedFlow<Unit>()

    override fun onPermissionStatusRefreshed() {
        refreshFlow.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val location: StateFlow<Location?> =
        combine(
            appPreferencesStore.getLiveLocationMinimumDistanceUpdateFlow(),
            refreshFlow.onStart { emit(Unit) },
        ) { minDistanceMeters, _ -> minDistanceMeters }
            .flatMapLatest { minDistanceMeters ->
                createLocationProvider(minDistanceMeters = minDistanceMeters).location
            }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = null
            )

    private fun createLocationProvider(
        minDistanceMeters: Int,
        updateInterval: Duration = 20.seconds,
        desiredAccuracy: DesiredAccuracy = DesiredAccuracy.Balanced,
    ): LocationProvider {
        return try {
            AndroidLocationProvider(
                context = context,
                updateInterval = updateInterval,
                desiredAccuracy = desiredAccuracy,
                minDistanceMeters = minDistanceMeters.toFloat(),
                coroutineScope = coroutineScope
            )
        } catch (_: PermissionException) {
            Timber.d("Permissions not granted, return NullLocationProvider.")
            NullLocationProvider()
        }
    }
}
