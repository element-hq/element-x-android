/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property.ICON_ANCHOR_CENTER
import org.maplibre.android.style.layers.Property.TEXT_ANCHOR_CENTER
import org.maplibre.android.style.layers.Property.TEXT_JUSTIFY_CENTER

@Composable
@MapLibreMapComposable
public fun TextSymbol(
    center: LatLng,
    size: Float = 1F,
    color: String = "",
    isDraggable: Boolean = false,
    zIndex: Int = 0,
    imageId: Int? = org.maplibre.android.R.drawable.maplibre_marker_icon_default,
    imageAnchor: String = ICON_ANCHOR_CENTER,
    imageOffset: Array<Float> = arrayOf(0f, 0f),
    imageRotation: Float? = null,
    text: String? = null,
    textAnchor: String = TEXT_ANCHOR_CENTER,
    textJustify: String = TEXT_JUSTIFY_CENTER,
    textOffset: Array<Float> = arrayOf(0f, 3f),
    textColor: String = "#000000",
    textHaloColor: String = "#000000",
    textHaloWidth: Float = 0f,
    onSymbolDragged: (LatLng) -> Unit = {},
    onDragFinished: (LatLng) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as MapApplier

    imageId?.let {
        if (mapApplier.style.getImage("$imageId") == null) {
            mapApplier.style.addImage(
                "$imageId",
                ImageBitmap.imageResource(it).asAndroidBitmap()
            )
        }
    }

    ComposeNode<MapApplier.SymbolNode, MapApplier>(factory = {
        val symbolManager = mapApplier.getOrCreateSymbolManagerForZIndex(zIndex)
        var symbolOptions = SymbolOptions()
            .withDraggable(isDraggable)
            .withLatLng(center)

        imageId?.let {
            symbolOptions = symbolOptions
                .withIconImage(imageId.toString())
                .withIconColor(color)
                .withIconSize(size)
                .withIconAnchor(imageAnchor)
                .withIconRotate(imageRotation)
                .withIconOffset(imageOffset)
        }

        text?.let {
            symbolOptions = symbolOptions
                .withTextField(text)
                .withTextColor(textColor)
                .withTextHaloColor(textHaloColor)
                .withTextHaloWidth(textHaloWidth)
                .withTextSize(size)
                .withTextJustify(textJustify)
                .withTextAnchor(textAnchor)
                .withTextOffset(textOffset)
        }

        val symbol = symbolManager.create(symbolOptions)

        MapApplier.SymbolNode(
            symbolManager,
            symbol,
            onSymbolDragged = { onSymbolDragged(it.latLng) },
            onSymbolDragStopped = { onDragFinished(it.latLng) },
        )
    }, update = {
        set(center) {
            symbol.latLng = center
            symbolManager.update(symbol)
        }

        set(text) {
            symbol.textField = text
            symbolManager.update(symbol)
        }

        set(color) {
            symbol.iconColor = color
        }

        set(imageRotation) {
            symbol.iconRotate = imageRotation
        }
    })
}
