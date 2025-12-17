/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import android.location.Location
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.parcelize.Parcelize
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Projection

/**
 * Create and [rememberSaveable] a [CameraPositionState] using [CameraPositionState.Saver].
 * [init] will be called when the [CameraPositionState] is first created to configure its
 * initial state.
 */
@Composable
public inline fun rememberCameraPositionState(
    crossinline init: CameraPositionState.() -> Unit = {}
): CameraPositionState = rememberSaveable(saver = CameraPositionState.Saver) {
    CameraPositionState().apply(init)
}

/**
 * A state object that can be hoisted to control and observe the map's camera state.
 * A [CameraPositionState] may only be used by a single [MapLibreMap] composable at a time
 * as it reflects instance state for a single view of a map.
 *
 * @param position the initial camera position
 * @param cameraMode the initial camera mode
 */
public class CameraPositionState(
    position: CameraPosition = CameraPosition.Builder().build(),
    cameraMode: CameraMode = CameraMode.NONE,
) {
    /**
     * Whether the camera is currently moving or not. This includes any kind of movement:
     * panning, zooming, or rotation.
     */
    public var isMoving: Boolean by mutableStateOf(false)
        internal set

    /**
     * The reason for the start of the most recent camera moment, or
     * [CameraMoveStartedReason.NO_MOVEMENT_YET] if the camera hasn't moved yet or
     * [CameraMoveStartedReason.UNKNOWN] if an unknown constant is received from the Maps SDK.
     */
    public var cameraMoveStartedReason: CameraMoveStartedReason by mutableStateOf(
        CameraMoveStartedReason.NO_MOVEMENT_YET
    )
        internal set

    /**
     * Returns the current [Projection] to be used for converting between screen
     * coordinates and lat/lng.
     */
    public val projection: Projection?
        get() = map?.projection

    /**
     * Local source of truth for the current camera position.
     * While [map] is non-null this reflects the current position of [map] as it changes.
     * While [map] is null it reflects the last known map position, or the last value set by
     * explicitly setting [position].
     */
    internal var rawPosition by mutableStateOf(position)

    /**
     * Current position of the camera on the map.
     */
    public var position: CameraPosition
        get() = rawPosition
        set(value) {
            synchronized(lock) {
                val map = map
                if (map == null) {
                    rawPosition = value
                } else {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(value))
                }
            }
        }

    /**
     * Local source of truth for the current camera mode.
     * While [map] is non-null this reflects the current camera mode as it changes.
     * While [map] is null it reflects the last known camera mode, or the last value set by
     * explicitly setting [cameraMode].
     */
    internal var rawCameraMode by mutableStateOf(cameraMode)

    /**
     * Current tracking mode of the camera.
     */
    public var cameraMode: CameraMode
        get() = rawCameraMode
        set(value) {
            synchronized(lock) {
                val map = map
                if (map == null) {
                    rawCameraMode = value
                } else {
                    map.locationComponent.cameraMode = value.toInternal()
                }
            }
        }

    /**
     * The user's last available location.
     */
    public var location: Location? by mutableStateOf(null)
        internal set

    // Used to perform side effects thread-safely.
    // Guards all mutable properties that are not `by mutableStateOf`.
    private val lock = Unit

    // The map currently associated with this CameraPositionState.
    // Guarded by `lock`.
    private var map: MapLibreMap? by mutableStateOf(null)

    // The current map is set and cleared by side effect.
    // There can be only one associated at a time.
    internal fun setMap(map: MapLibreMap?) {
        synchronized(lock) {
            if (this.map == null && map == null) return
            if (this.map != null && map != null) {
                error("CameraPositionState may only be associated with one MapLibreMap at a time")
            }
            this.map = map
            if (map == null) {
                isMoving = false
            } else {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                map.locationComponent.cameraMode = cameraMode.toInternal()
            }
        }
    }

    public companion object {
        /**
         * The default saver implementation for [CameraPositionState].
         */
        public val Saver: Saver<CameraPositionState, SaveableCameraPositionData> = Saver(
            save = { SaveableCameraPositionData(it.position, it.cameraMode.toInternal()) },
            restore = { CameraPositionState(it.position, CameraMode.fromInternal(it.cameraMode)) }
        )
    }
}

/** Provides the [CameraPositionState] used by the map. */
internal val LocalCameraPositionState = staticCompositionLocalOf { CameraPositionState() }

/** The current [CameraPositionState] used by the map. */
public val currentCameraPositionState: CameraPositionState
    @[MapLibreMapComposable ReadOnlyComposable Composable]
    get() = LocalCameraPositionState.current

@Parcelize
public data class SaveableCameraPositionData(
    val position: CameraPosition,
    val cameraMode: Int
) : Parcelable
