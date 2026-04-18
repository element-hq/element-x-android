/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.Location
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.rememberLocationPinBitmap
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.dsl.and
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

private const val LOCATION_MARKER_ID = "LOCATION_MARKER_ID"

/**
 * Data class representing a marker on the map.
 *
 * @param id Unique identifier for the marker
 * @param location The geographic location of the marker
 * @param variant The visual variant of the pin (user location, pinned, stale)
 */
data class LocationMarkerData(
    val id: String,
    val location: Location,
    val variant: PinVariant,
)

/**
 * A composable that renders location markers on a MapLibre map with clustering support.
 *
 * Uses GeoJSON source with clustering enabled to group nearby markers.
 * Individual markers are rendered using Canvas-based pin rendering with Coil for avatar loading.
 * Clusters are rendered as circles with point counts.
 *
 * Must be used within a MaplibreMap content block.
 *
 * @param markers List of markers to display on the map
 * @param onMarkerClick Callback when a marker is clicked
 * @param onClusterClick Callback when a cluster is clicked, provides cluster center position
 */
@Composable
fun LocationPinMarkers(
    markers: List<LocationMarkerData>,
    onMarkerClick: ((LocationMarkerData) -> Unit)? = null,
    onClusterClick: ((Position) -> Unit)? = null,
) {
    if (markers.isEmpty()) return
    val clusterColor = ElementTheme.colors.bgAccentRest
    val clusterStrokeColor = ElementTheme.colors.iconOnSolidPrimary
    val clusterTextColor = ElementTheme.colors.textOnSolidPrimary
    val clusterTextStyle = ElementTheme.typography.fontBodyMdMedium

    // Convert markers to GeoJSON
    val geoJsonString = remember(markers) {
        val features = markers.map { marker ->
            Feature(
                id = JsonPrimitive(marker.id),
                geometry = Point(Position(marker.location.lon, marker.location.lat)),
                properties = mapOf(
                    LOCATION_MARKER_ID to JsonPrimitive(marker.id),
                )
            )
        }
        FeatureCollection(features).toJson()
    }

    // Create GeoJSON source with clustering
    val markersSource = rememberGeoJsonSource(
        data = GeoJsonData.JsonString(geoJsonString),
        options = GeoJsonOptions(
            cluster = true,
            clusterMinPoints = 3,
            clusterRadius = 30
        ),
    )

    // Cluster circle layer
    CircleLayer(
        id = "cluster-circles",
        source = markersSource,
        filter = feature.has("point_count"),
        color = const(clusterColor),
        radius = const(24.dp),
        strokeWidth = const(1.dp),
        strokeColor = const(clusterStrokeColor),
        onClick = { features ->
            features.firstOrNull()?.let { feat ->
                val point = feat.geometry as? Point
                if (point != null && onClusterClick != null) {
                    onClusterClick(point.coordinates)
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            } ?: ClickResult.Pass
        },
    )

    // Cluster count text layer
    SymbolLayer(
        id = "cluster-count",
        source = markersSource,
        filter = feature.has("point_count"),
        textField = feature["point_count_abbreviated"].asString(),
        textColor = const(clusterTextColor),
        textSize = const(clusterTextStyle.fontSize),
        textFont = const(listOfNotNull(clusterTextStyle.fontFamily?.toString())),
        textLetterSpacing = const(clusterTextStyle.letterSpacing),
    )

    // Individual marker layers - one per marker for unique avatars
    markers.forEach { marker ->
        LocationPinMarkerLayer(
            marker = marker,
            source = markersSource,
            onMarkerClick = onMarkerClick,
        )
    }
}

@Composable
private fun LocationPinMarkerLayer(
    marker: LocationMarkerData,
    source: GeoJsonSource,
    onMarkerClick: ((LocationMarkerData) -> Unit)?,
) {
    val imageBitmap = rememberLocationPinBitmap(marker.variant)
    if (imageBitmap != null) {
        SymbolLayer(
            id = "pin-marker-${marker.id}",
            source = source,
            filter = !feature.has("point_count") and (feature[LOCATION_MARKER_ID].asString() eq const(marker.id)),
            iconImage = image(imageBitmap),
            iconAnchor = const(SymbolAnchor.Bottom),
            iconAllowOverlap = const(true),
            onClick = { features ->
                if (features.isNotEmpty() && onMarkerClick != null) {
                    onMarkerClick(marker)
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            },
        )
    }
}
