/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.awaitCancellation
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A compose container for a MapLibre [MapView].
 *
 * Heavily inspired by https://github.com/googlemaps/android-maps-compose
 *
 * @param styleUri a URI where to asynchronously fetch a style for the map
 * @param modifier Modifier to be applied to the MapLibreMap
 * @param images images added to the map's style to be later used with [Symbol]
 * @param cameraPositionState the [CameraPositionState] to be used to control or observe the map's
 * camera state
 * @param uiSettings the [MapUiSettings] to be used for UI-specific settings on the map
 * @param symbolManagerSettings the [MapSymbolManagerSettings] to be used for symbol manager settings
 * @param locationSettings the [MapLocationSettings] to be used for location settings
 * @param content the content of the map
 */
@Composable
public fun MapLibreMap(
    styleUri: String,
    modifier: Modifier = Modifier,
    images: ImmutableMap<String, Int> = persistentMapOf(),
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    symbolManagerSettings: MapSymbolManagerSettings = DefaultMapSymbolManagerSettings,
    locationSettings: MapLocationSettings = DefaultMapLocationSettings,
    content: (@Composable @MapLibreMapComposable () -> Unit)? = null,
) {
    // When in preview, early return a Box with the received modifier preserving layout
    if (LocalInspectionMode.current) {
        @Suppress("ModifierReused") // False positive, the modifier is not reused due to the early return.
        Box(
            modifier = modifier.background(Color.DarkGray)
        ) {
            Text("[Map]", modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    @Suppress("ModifierReused")
    AndroidView(modifier = modifier, factory = { mapView })
    MapLifecycle(mapView)

    // rememberUpdatedState and friends are used here to make these values observable to
    // the subcomposition without providing a new content function each recomposition
    val currentCameraPositionState by rememberUpdatedState(cameraPositionState)
    val currentUiSettings by rememberUpdatedState(uiSettings)
    val currentMapLocationSettings by rememberUpdatedState(locationSettings)
    val currentSymbolManagerSettings by rememberUpdatedState(symbolManagerSettings)

    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    LaunchedEffect(styleUri, images) {
        disposingComposition {
            parentComposition.newComposition(
                context = context,
                mapView = mapView,
                styleUri = styleUri,
                images = images,
            ) {
                MapUpdater(
                    cameraPositionState = currentCameraPositionState,
                    uiSettings = currentUiSettings,
                    locationSettings = currentMapLocationSettings,
                    symbolManagerSettings = currentSymbolManagerSettings,
                )
                CompositionLocalProvider(
                    LocalCameraPositionState provides cameraPositionState,
                ) {
                    currentContent?.invoke()
                }
            }
        }
    }
}

private suspend inline fun disposingComposition(factory: () -> Composition) {
    val composition = factory()
    try {
        awaitCancellation()
    } finally {
        composition.dispose()
    }
}

private suspend inline fun CompositionContext.newComposition(
    context: Context,
    mapView: MapView,
    styleUri: String,
    images: ImmutableMap<String, Int>,
    noinline content: @Composable () -> Unit
): Composition {
    val map = mapView.awaitMap()
    val style = map.awaitStyle(context, styleUri, images)
    val symbolManager = SymbolManager(mapView, map, style)
    return Composition(
        MapApplier(map, style, symbolManager),
        this
    ).apply {
        setContent(content)
    }
}

private suspend inline fun MapView.awaitMap(): MapLibreMap = suspendCoroutine { continuation ->
    getMapAsync { map ->
        continuation.resume(map)
    }
}

private suspend inline fun MapLibreMap.awaitStyle(
    context: Context,
    styleUri: String,
    images: ImmutableMap<String, Int>,
): Style = suspendCoroutine { continuation ->
    setStyle(
        Style.Builder().apply {
            fromUri(styleUri)
            images.forEach { (id, drawableRes) ->
                withImage(id, checkNotNull(context.getDrawable(drawableRes)) {
                    "Drawable resource $drawableRes with id $id not found"
                })
            }
        }
    ) { style ->
        continuation.resume(style)
    }
}

/**
 * Registers lifecycle observers to the local [MapView].
 */
@Composable
private fun MapLifecycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(previousState)
        val callbacks = mapView.componentCallbacks()

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)

        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
        }
    }
    DisposableEffect(mapView) {
        onDispose {
            mapView.onDestroy()
            mapView.removeAllViews()
        }
    }
}

private fun MapView.lifecycleObserver(previousState: MutableState<Lifecycle.Event>): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        event.targetState
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                // Skip calling mapView.onCreate if the lifecycle did not go through onDestroy - in
                // this case the MapLibreMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
            }
            Lifecycle.Event.ON_START -> this.onStart()
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_STOP -> this.onStop()
            Lifecycle.Event.ON_DESTROY -> {
                // handled in onDispose
            }
            Lifecycle.Event.ON_ANY -> error("ON_ANY should never be used")
        }
        previousState.value = event
    }

private fun MapView.componentCallbacks(): ComponentCallbacks2 =
    object : ComponentCallbacks2 {
        override fun onConfigurationChanged(config: Configuration) = Unit

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onLowMemory() = Unit

        override fun onTrimMemory(level: Int) {
            // We call the `MapView.onLowMemory` method for any memory trim level
            this@componentCallbacks.onLowMemory()
        }
    }
