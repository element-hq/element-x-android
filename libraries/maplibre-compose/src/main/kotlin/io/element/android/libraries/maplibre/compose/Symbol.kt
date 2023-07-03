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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions

internal class SymbolNode(
    val symbolManager: SymbolManager,
    val symbolState: SymbolState,
    val symbol: Symbol,
) : MapNode {
    override fun onAttached() {
        symbolState.symbol = symbol
    }

    override fun onRemoved() {
        symbolState.symbol = null
        symbolManager.delete(symbol)
        // style.removeImage(uuid) // TODO: Dynamically remove images.
    }

    override fun onCleared() {
        symbolState.symbol = null
        symbolManager.delete(symbol)
        // style.removeImage(uuid) // TODO: Dynamically remove images.
    }
}

/**
 * A state object that can be hoisted to control and observe the symbol state.
 *
 * @param position the initial symbol position
 */
public class SymbolState(
    position: LatLng = LatLng(0.0, 0.0)
) {
    /**
     * Current position of the symbol.
     */
    public var position: LatLng by mutableStateOf(position)

    // The symbol associated with this SymbolState.
    private val symbolState: MutableState<Symbol?> = mutableStateOf(null)
    internal var symbol: Symbol?
        get() = symbolState.value
        set(value) {
            if (symbolState.value == null && value == null) return
            if (symbolState.value != null && value != null) {
                error("SymbolState may only be associated with one Symbol at a time.")
            }
            symbolState.value = value
        }

    public companion object {
        /**
         * The default saver implementation for [SymbolState]
         */
        public val Saver: Saver<SymbolState, LatLng> = Saver(
            save = { it.position },
            restore = { SymbolState(it) }
        )
    }
}

@Composable
public fun rememberSymbolState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): SymbolState = rememberSaveable(key = key, saver = SymbolState.Saver) {
    SymbolState(position)
}

/**
 * A composable for a symbol on the map.
 *
 * @param state the [SymbolState] to be used to control or observe the symbol
 * state such as its position and info window
 * @param alpha the alpha (opacity) of the symbol
 * @param iconAnchor the anchor for the symbol image
 */
@Composable
@MapboxMapComposable
public fun Symbol(
    state: SymbolState = rememberSymbolState(),
    iconId: String,
    iconAnchor: IconAnchor? = null,
) {
    val mapApplier = currentComposer.applier as MapApplier
    val symbolManager = mapApplier.symbolManager
    ComposeNode<SymbolNode, MapApplier>(
        factory = {
            // style.addImage(uuid, icon) // TODO Dynamically add images
            SymbolNode(
                symbolManager = symbolManager,
                symbolState = state,
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
            }
            update(iconAnchor) {
                this.symbol.iconAnchor = it?.toInternal()
            }
        }
    )
}
