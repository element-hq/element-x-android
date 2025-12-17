/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

internal class SymbolNode(
    val symbolManager: SymbolManager,
    val symbol: Symbol,
) : MapNode {
    override fun onRemoved() {
        symbolManager.delete(symbol)
    }

    override fun onCleared() {
        symbolManager.delete(symbol)
    }
}

/**
 * A state object that can be hoisted to control and observe the symbol state.
 *
 * @param position the initial symbol position
 */
public class SymbolState(
    position: LatLng
) {
    /**
     * Current position of the symbol.
     */
    public var position: LatLng by mutableStateOf(position)

    public companion object {
        /**
         * The default saver implementation for [SymbolState].
         */
        public val Saver: Saver<SymbolState, LatLng> = Saver(
            save = { it.position },
            restore = { SymbolState(it) }
        )
    }
}

@Composable
public fun rememberSymbolState(
    position: LatLng = LatLng(0.0, 0.0)
): SymbolState = rememberSaveable(saver = SymbolState.Saver) {
    SymbolState(position)
}

/**
 * A composable for a symbol on the map.
 *
 * @param iconId an id of an image from the current [Style]
 * @param state the [SymbolState] to be used to control or observe the symbol
 * state such as its position and info window
 * @param iconAnchor the anchor for the symbol image
 */
@Composable
@MapLibreMapComposable
public fun Symbol(
    iconId: String,
    state: SymbolState = rememberSymbolState(),
    iconAnchor: IconAnchor? = null,
) {
    val mapApplier = currentComposer.applier as MapApplier
    val symbolManager = mapApplier.symbolManager
    ComposeNode<SymbolNode, MapApplier>(
        factory = {
            SymbolNode(
                symbolManager = symbolManager,
                symbol = symbolManager.create(
                    SymbolOptions().apply {
                        withLatLng(state.position)
                        withIconImage(iconId)
                        iconAnchor?.let { withIconAnchor(it.toInternal()) }
                    }
                ),
            )
        },
        update = {
            update(state.position) {
                this.symbol.latLng = it
                symbolManager.update(this.symbol)
            }
            update(iconId) {
                this.symbol.iconImage = it
                symbolManager.update(this.symbol)
            }
            update(iconAnchor) {
                this.symbol.iconAnchor = it?.toInternal()
                symbolManager.update(this.symbol)
            }
        }
    )
}
