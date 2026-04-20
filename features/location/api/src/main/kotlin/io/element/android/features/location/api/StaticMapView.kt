/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.Extras
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.internal.StaticMapPlaceholder
import io.element.android.features.location.api.internal.StaticMapUrlBuilder
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.libraries.designsystem.components.LocationPin
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Shows a static map image downloaded via a third party service's static maps API.
 *
 * Handles 4 distinct cases:
 * 1. Stale location (pinVariant is StaleLocation) - shows stale map with stale pin, no fetching
 * 2. Null location - shows blurred placeholder, no pin, no loading
 * 3. Loading (location != null, fetching) - shows blurred placeholder with loading indicator
 * 4. Success (location != null, loaded) - shows actual map with pin
 */
@Composable
fun StaticMapView(
    location: Location?,
    zoom: Double,
    pinVariant: PinVariant,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    darkMode: Boolean = !ElementTheme.isLightTheme,
) {
    // Using BoxWithConstraints to:
    // 1) Size the inner Image to the same Dp size of the outer BoxWithConstraints.
    // 2) Request the static map image of the exact required size in Px to fill the AsyncImage.
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Case 1: Stale location - show stale map with stale pin, no fetching
        when {
            pinVariant is PinVariant.StaleLocation -> {
                StaleMapContent(
                    pinVariant = pinVariant,
                    contentDescription = contentDescription,
                    width = maxWidth,
                    height = maxHeight,
                )
            }
            // Case 2: Null location - show blurred placeholder, no pin, no loading
            location == null -> {
                StaticMapPlaceholder(
                    painter = painterResource(R.drawable.blurred_map),
                    canReload = false,
                    contentDescription = contentDescription,
                    width = maxWidth,
                    height = maxHeight,
                    onLoadMapClick = {}
                )
            }
            // Cases 3 & 4: Non-null location - fetch map
            else -> LoadableMapContent(
                location = location,
                zoom = zoom,
                pinVariant = pinVariant,
                contentDescription = contentDescription,
                darkMode = darkMode,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.StaleMapContent(
    pinVariant: PinVariant,
    contentDescription: String?,
    width: Dp,
    height: Dp,
) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.stale_map),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width = width, height = height)
        )
        LocationPin(variant = pinVariant, modifier = Modifier.centerBottomEdge(this@StaleMapContent))
    }
}

@Composable
private fun BoxWithConstraintsScope.LoadableMapContent(
    location: Location,
    zoom: Double,
    pinVariant: PinVariant,
    contentDescription: String?,
    darkMode: Boolean,
) {
    val context = LocalContext.current
    var retryHash by remember { mutableIntStateOf(0) }
    val builder = remember { StaticMapUrlBuilder() }

    val painter = rememberAsyncImagePainter(
        model = if (constraints.isZero) {
            // Avoid building a URL if any of the size constraints is zero
            null
        } else {
            ImageRequest.Builder(context)
                .data(
                    builder.build(
                        lat = location.lat,
                        lon = location.lon,
                        zoom = zoom,
                        darkMode = darkMode,
                        width = constraints.maxWidth,
                        height = constraints.maxHeight,
                        density = LocalDensity.current.density,
                    )
                )
                .size(width = constraints.maxWidth, height = constraints.maxHeight)
                .apply {
                    extras.set(Extras.Key("retry_hash"), retryHash).build()
                }
                .build()
        }
    )

    val state by painter.state.collectAsState()
    when (state) {
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(width = maxWidth, height = maxHeight),
                // The returned image can be smaller than the requested size due to the static maps API having
                // a max width and height of 2048 px. We apply ContentScale.Fit to handle this.
                contentScale = ContentScale.Fit,
            )
            LocationPin(variant = pinVariant, modifier = Modifier.centerBottomEdge(this))
        }
        else -> {
            StaticMapPlaceholder(
                painter = painterResource(R.drawable.blurred_map),
                canReload = builder.isServiceAvailable() && state is AsyncImagePainter.State.Error,
                contentDescription = contentDescription,
                width = maxWidth,
                height = maxHeight,
                onLoadMapClick = { retryHash++ }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun StaticMapViewPreview() = ElementPreview {
    StaticMapView(
        location = Location(0.0, 0.0),
        zoom = 0.0,
        contentDescription = null,
        pinVariant = PinVariant.PinnedLocation,
        modifier = Modifier.size(400.dp),
    )
}
