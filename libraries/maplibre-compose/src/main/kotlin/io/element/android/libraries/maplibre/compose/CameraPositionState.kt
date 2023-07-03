/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.maplibre.compose

import android.location.Location
import android.os.Parcelable
import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Projection
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.parcelize.Parcelize
import java.lang.Integer.MAX_VALUE
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Create and [rememberSaveable] a [CameraPositionState] using [CameraPositionState.Saver].
 * [init] will be called when the [CameraPositionState] is first created to configure its
 * initial state.
 */
@Composable
public inline fun rememberCameraPositionState(
    key: String? = null,
    crossinline init: CameraPositionState.() -> Unit = {}
): CameraPositionState = rememberSaveable(key = key, saver = CameraPositionState.Saver) {
    CameraPositionState().apply(init)
}

/**
 * A state object that can be hoisted to control and observe the map's camera state.
 * A [CameraPositionState] may only be used by a single [MapboxMap] composable at a time
 * as it reflects instance state for a single view of a map.
 *
 * @param position the initial camera position
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
     * The user's last available location
     */
    public var location: Location? by mutableStateOf(null)
        internal set

    // Used to perform side effects thread-safely.
    // Guards all mutable properties that are not `by mutableStateOf`.
    private val lock = Unit

    // The map currently associated with this CameraPositionState.
    // Guarded by `lock`.
    private var map: MapboxMap? by mutableStateOf(null)

    // An action to run when the map becomes available or unavailable.
    // represents a mutually exclusive mutation to perform while holding `lock`.
    // Guarded by `lock`.
    private var onMapChanged: OnMapChangedCallback? by mutableStateOf(null)

    /**
     * Set [onMapChanged] to [callback], invoking the current callback's
     * [OnMapChangedCallback.onCancelLocked] if one is present.
     */
    private fun doOnMapChangedLocked(callback: OnMapChangedCallback) {
        onMapChanged?.onCancelLocked()
        onMapChanged = callback
    }

    // A token representing the current owner of any ongoing motion in progress.
    // Used to determine if map animation should stop when calls to animate end.
    // Guarded by `lock`.
    private var movementOwner: Any? by mutableStateOf(null)

    /**
     * Used with [onMapChangedLocked] to execute one-time actions when a map becomes available
     * or is made unavailable. Cancellation is provided in order to resume suspended coroutines
     * that are awaiting the execution of one of these callbacks that will never come.
     */
    private fun interface OnMapChangedCallback {
        fun onMapChangedLocked(newMap: MapboxMap?)
        fun onCancelLocked() {}
    }

    // The current map is set and cleared by side effect.
    // There can be only one associated at a time.
    internal fun setMap(map: MapboxMap?) {
        synchronized(lock) {
            if (this.map == null && map == null) return
            if (this.map != null && map != null) {
                error("CameraPositionState may only be associated with one GoogleMap at a time")
            }
            this.map = map
            if (map == null) {
                isMoving = false
            } else {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                map.locationComponent.cameraMode = cameraMode.toInternal()
            }
            onMapChanged?.let {
                // Clear this first since the callback itself might set it again for later
                onMapChanged = null
                it.onMapChangedLocked(map)
            }
        }
    }

    /**
     * Animate the camera position as specified by [update], returning once the animation has
     * completed. [position] will reflect the position of the camera as the animation proceeds.
     *
     * [animate] will throw [CancellationException] if the animation does not fully complete.
     * This can happen if:
     *
     * * The user manipulates the map directly
     * * [position] is set explicitly, e.g. `state.position = CameraPosition(...)`
     * * [animate] is called again before an earlier call to [animate] returns
     * * [move] is called
     * * The calling job is [cancelled][kotlinx.coroutines.Job.cancel] externally
     *
     * If this [CameraPositionState] is not currently bound to a [GoogleMap] this call will
     * suspend until a map is bound and animation will begin.
     *
     * This method should only be called from a dispatcher bound to the map's UI thread.
     *
     * @param update the change that should be applied to the camera
     * @param durationMs The duration of the animation in milliseconds. If [Int.MAX_VALUE] is
     * provided, the default animation duration will be used. Otherwise, the value provided must be
     * strictly positive, otherwise an [IllegalArgumentException] will be thrown.
     */
    @UiThread
    public suspend fun animate(update: CameraUpdate, durationMs: Int = MAX_VALUE) {
        val myJob = currentCoroutineContext()[Job]
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                synchronized(lock) {
                    movementOwner = myJob
                    val map = map
                    if (map == null) {
                        // Do it later
                        val animateOnMapAvailable = object : OnMapChangedCallback {
                            override fun onMapChangedLocked(newMap: MapboxMap?) {
                                if (newMap == null) {
                                    // Cancel the animate caller and crash the map setter
                                    @Suppress("ThrowableNotThrown")
                                    continuation.resumeWithException(
                                        CancellationException(
                                            "internal error; no GoogleMap available"
                                        )
                                    )
                                    error(
                                        "internal error; no GoogleMap available to animate position"
                                    )
                                }
                                performAnimateCameraLocked(newMap, update, durationMs, continuation)
                            }

                            override fun onCancelLocked() {
                                continuation.resumeWithException(
                                    CancellationException("Animation cancelled")
                                )
                            }
                        }
                        doOnMapChangedLocked(animateOnMapAvailable)
                        continuation.invokeOnCancellation {
                            synchronized(lock) {
                                if (onMapChanged === animateOnMapAvailable) {
                                    // External cancellation shouldn't invoke onCancel
                                    // so we set this to null directly instead of going through
                                    // doOnMapChangedLocked(null).
                                    onMapChanged = null
                                }
                            }
                        }
                    } else {
                        performAnimateCameraLocked(map, update, durationMs, continuation)
                    }
                }
            }
        } finally {
            // continuation.invokeOnCancellation might be called from any thread, so stop the
            // animation in progress here where we're guaranteed to be back on the right dispatcher.
            synchronized(lock) {
                if (myJob != null && movementOwner === myJob) {
                    movementOwner = null
                    map?.cancelTransitions()
                }
            }
        }
    }

    private fun performAnimateCameraLocked(
        map: MapboxMap,
        update: CameraUpdate,
        durationMs: Int,
        continuation: CancellableContinuation<Unit>
    ) {
        val cancelableCallback = object : MapboxMap.CancelableCallback {
            override fun onCancel() {
                continuation.resumeWithException(CancellationException("Animation cancelled"))
            }

            override fun onFinish() {
                continuation.resume(Unit)
            }
        }
        if (durationMs == MAX_VALUE) {
            map.animateCamera(update, cancelableCallback)
        } else {
            map.animateCamera(update, durationMs, cancelableCallback)
        }
        doOnMapChangedLocked {
            check(it == null) {
                "New GoogleMap unexpectedly set while an animation was still running"
            }
            map.cancelTransitions()
        }
    }

    /**
     * Move the camera instantaneously as specified by [update]. Any calls to [animate] in progress
     * will be cancelled. [position] will be updated when the bound map's position has been updated,
     * or if the map is currently unbound, [update] will be applied when a map is next bound.
     * Other calls to [move], [animate], or setting [position] will override an earlier pending
     * call to [move].
     *
     * This method must be called from the map's UI thread.
     */
    @UiThread
    public fun move(update: CameraUpdate) {
        synchronized(lock) {
            val map = map
            movementOwner = null
            if (map == null) {
                // Do it when we have a map available
                doOnMapChangedLocked { it?.moveCamera(update) }
            } else {
                map.moveCamera(update)
            }
        }
    }

    public companion object {
        /**
         * The default saver implementation for [CameraPositionState]
         */
        public val Saver: Saver<CameraPositionState, SaveableCameraPositionState> = Saver(
            save = { SaveableCameraPositionState(it.position, it.cameraMode.toInternal()) },
            restore = { CameraPositionState(it.position, CameraMode.fromInternal(it.cameraMode)) }
        )
    }
}

/** Provides the [CameraPositionState] used by the map. */
internal val LocalCameraPositionState = staticCompositionLocalOf { CameraPositionState() }

/** The current [CameraPositionState] used by the map. */
public val currentCameraPositionState: CameraPositionState
    @[MapboxMapComposable ReadOnlyComposable Composable]
    get() = LocalCameraPositionState.current

@Parcelize
public data class SaveableCameraPositionState(
    val position: CameraPosition,
    val cameraMode: Int
) : Parcelable
