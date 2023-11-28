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

package io.element.android.features.location.api

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.internal.StaticMapPlaceholder
import io.element.android.features.location.api.internal.StaticMapUrlBuilder
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.CommonDrawables
import timber.log.Timber

/**
 * Shows a static map image downloaded via a third party service's static maps API.
 */
@Composable
fun StaticMapView(
    lat: Double,
    lon: Double,
    zoom: Double,
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
        val context = LocalContext.current
        var retryHash by remember { mutableIntStateOf(0) }
        val builder = remember { StaticMapUrlBuilder(context) }
        val painter = rememberAsyncImagePainter(
            model = if (constraints.isZero) {
                // Avoid building a URL if any of the size constraints is zero (else it will thrown an exception).
                null
            } else {
                ImageRequest.Builder(context)
                    .data(
                        builder.build(
                            lat = lat,
                            lon = lon,
                            zoom = zoom,
                            darkMode = darkMode,
                            width = constraints.maxWidth,
                            height = constraints.maxHeight,
                            density = LocalDensity.current.density,
                        )
                    )
                    .size(width = constraints.maxWidth, height = constraints.maxHeight)
                    .setParameter("retry_hash", retryHash, memoryCacheKey = null)
                    .build()
            }.apply {
                Timber.d("Static map image request: ${this?.data}")
            }
        )

        if (painter.state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(width = maxWidth, height = maxHeight),
                // The returned image can be smaller than the requested size due to the static maps API having
                // a max width and height of 2048 px. See buildStaticMapsApiUrl() for more details.
                // We apply ContentScale.Fit to scale the image to fill the AsyncImage should this be the case.
                contentScale = ContentScale.Fit,
            )
            Icon(
                resourceId = CommonDrawables.pin,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.centerBottomEdge(this),
            )
        } else {
            StaticMapPlaceholder(
                showProgress = painter.state is AsyncImagePainter.State.Loading,
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
        lat = 0.0,
        lon = 0.0,
        zoom = 0.0,
        contentDescription = null,
        modifier = Modifier.size(400.dp),
    )
}
