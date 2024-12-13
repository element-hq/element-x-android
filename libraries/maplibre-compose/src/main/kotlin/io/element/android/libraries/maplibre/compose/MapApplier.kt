/*
 * Copyright 2023, 2024 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.AbstractApplier
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.AnnotationManager
import org.maplibre.android.plugins.annotation.Circle
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.Fill
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.OnCircleDragListener
import org.maplibre.android.plugins.annotation.OnSymbolDragListener
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import kotlin.math.abs

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

internal class MapApplier(
    val map: MapLibreMap,
    val style: Style,
    private val mapView: MapView,
) : AbstractApplier<MapNode>(MapNodeRoot) {
    private val decorations = mutableListOf<MapNode>()

    private val circleManagerMap = mutableMapOf<Int, CircleManager>()
    private val fillManagerMap = mutableMapOf<Int, FillManager>()
    private val symbolManagerMap = mutableMapOf<Int, SymbolManager>()
    private val lineManagerMap = mutableMapOf<Int, LineManager>()

    private val zIndexReferenceAnnotationManagerMap =
        mutableMapOf<Int, AnnotationManager<*, *, *, *, *, *>>()

    fun getOrCreateCircleManagerForZIndex(zIndex: Int): CircleManager {
        circleManagerMap[zIndex]?.let { return it }

        val layerInsertInfo = getLayerInsertInfoForZIndex(zIndex)

        val circleManager = layerInsertInfo?.let {
            CircleManager(
                mapView,
                map,
                style,
                if (it.insertPosition == LayerInsertMethod.INSERT_BELOW) it.referenceLayerId else null,
                if (it.insertPosition == LayerInsertMethod.INSERT_ABOVE) it.referenceLayerId else null
            )
        } ?: run {
            CircleManager(mapView, map, style)
        }

        circleManagerMap[zIndex] = circleManager

        if (!zIndexReferenceAnnotationManagerMap.containsKey(zIndex)) {
            zIndexReferenceAnnotationManagerMap[zIndex] = circleManager
        }

        circleManager.addDragListener(object : OnCircleDragListener {
            override fun onAnnotationDragStarted(annotation: Circle?) {
                decorations.findInputCallback<CircleNode, Circle, Unit>(
                    nodeMatchPredicate = { it.circle.id == annotation?.id && it.circleManager.layerId == circleManager.layerId },
                    nodeInputCallback = { onCircleDragged }
                )?.invoke(annotation!!)
            }

            override fun onAnnotationDrag(annotation: Circle?) {
                decorations.findInputCallback<CircleNode, Circle, Unit>(
                    nodeMatchPredicate = { it.circle.id == annotation?.id && it.circleManager.layerId == circleManager.layerId },
                    nodeInputCallback = { onCircleDragged }
                )?.invoke(annotation!!)
            }

            override fun onAnnotationDragFinished(annotation: Circle?) {
                decorations.findInputCallback<CircleNode, Circle, Unit>(
                    nodeMatchPredicate = { it.circle.id == annotation?.id && it.circleManager.layerId == circleManager.layerId },
                    nodeInputCallback = { onCircleDragStopped }
                )?.invoke(annotation!!)
            }
        })

        return circleManagerMap[zIndex]!!
    }

    private fun getLayerInsertInfoForZIndex(zIndex: Int): LayerInsertInfo? {
        val keys = zIndexReferenceAnnotationManagerMap.keys.sorted()

        if (keys.isEmpty()) {
            return null
        }

        val closestLayerIndex = keys.map {
            abs(it - zIndex)
        }.withIndex().minBy { it.value }.index

        return LayerInsertInfo(
            zIndexReferenceAnnotationManagerMap[keys[closestLayerIndex]]?.layerId!!,
            if (zIndex > keys[closestLayerIndex]) LayerInsertMethod.INSERT_ABOVE else LayerInsertMethod.INSERT_BELOW
        )
    }

    fun getOrCreateSymbolManagerForZIndex(zIndex: Int): SymbolManager {
        symbolManagerMap[zIndex]?.let { return it }
        val layerInsertInfo = getLayerInsertInfoForZIndex(zIndex)

        val symbolManager = layerInsertInfo?.let {
            SymbolManager(
                mapView,
                map,
                style,
                if (it.insertPosition == LayerInsertMethod.INSERT_BELOW) it.referenceLayerId else null,
                if (it.insertPosition == LayerInsertMethod.INSERT_ABOVE) it.referenceLayerId else null,
                null
            )
        } ?: run {
            SymbolManager(mapView, map, style)
        }

        symbolManager.iconAllowOverlap = true

        symbolManagerMap[zIndex] = symbolManager

        if (!zIndexReferenceAnnotationManagerMap.containsKey(zIndex)) {
            zIndexReferenceAnnotationManagerMap[zIndex] = symbolManager
        }

        symbolManager.addDragListener(object : OnSymbolDragListener {
            override fun onAnnotationDragStarted(annotation: Symbol?) {
                decorations.findInputCallback<SymbolNode, Symbol, Unit>(
                    nodeMatchPredicate = { it.symbol.id == annotation?.id && it.symbolManager.layerId == symbolManager.layerId },
                    nodeInputCallback = { onSymbolDragged }
                )?.invoke(annotation!!)
            }

            override fun onAnnotationDrag(annotation: Symbol?) {
                decorations.findInputCallback<SymbolNode, Symbol, Unit>(
                    nodeMatchPredicate = { it.symbol.id == annotation?.id && it.symbolManager.layerId == symbolManager.layerId },
                    nodeInputCallback = { onSymbolDragged }
                )?.invoke(annotation!!)
            }

            override fun onAnnotationDragFinished(annotation: Symbol?) {
                decorations.findInputCallback<SymbolNode, Symbol, Unit>(
                    nodeMatchPredicate = { it.symbol.id == annotation?.id && it.symbolManager.layerId == symbolManager.layerId },
                    nodeInputCallback = { onSymbolDragStopped }
                )?.invoke(annotation!!)
            }
        })

        return symbolManager
    }

    fun getOrCreateFillManagerForZIndex(zIndex: Int): FillManager {
        fillManagerMap[zIndex]?.let { return it }
        val layerInsertInfo = getLayerInsertInfoForZIndex(zIndex)
        val fillManager = layerInsertInfo?.let {
            FillManager(
                mapView,
                map,
                style,
                if (it.insertPosition == LayerInsertMethod.INSERT_BELOW) it.referenceLayerId else null,
                if (it.insertPosition == LayerInsertMethod.INSERT_ABOVE) it.referenceLayerId else null,
                null
            )
        } ?: run {
            FillManager(mapView, map, style)
        }

        if (!zIndexReferenceAnnotationManagerMap.containsKey(zIndex)) {
            zIndexReferenceAnnotationManagerMap[zIndex] = fillManager
        }

        fillManagerMap[zIndex] = fillManager
        return fillManager
    }

    fun getOrCreateLineManagerForZIndex(zIndex: Int): LineManager {
        lineManagerMap[zIndex]?.let { return it }
        val layerInsertInfo = getLayerInsertInfoForZIndex(zIndex)
        val lineManager = layerInsertInfo?.let {
            LineManager(
                mapView,
                map,
                style,
                if (it.insertPosition == LayerInsertMethod.INSERT_BELOW) it.referenceLayerId else null,
                if (it.insertPosition == LayerInsertMethod.INSERT_ABOVE) it.referenceLayerId else null,
                null
            )
        } ?: run {
            LineManager(mapView, map, style)
        }


        if (!zIndexReferenceAnnotationManagerMap.containsKey(zIndex)) {
            zIndexReferenceAnnotationManagerMap[zIndex] = lineManager
        }

        lineManagerMap[zIndex] = lineManager
        return lineManager
    }

    data class LayerInsertInfo(val referenceLayerId: String, val insertPosition: LayerInsertMethod)

    enum class LayerInsertMethod {
        INSERT_BELOW,
        INSERT_ABOVE
    }

    internal class CircleNode(
        val circleManager: CircleManager,
        val circle: Circle,
        var onCircleDragged: (Circle) -> Unit,
        var onCircleDragStopped: (Circle) -> Unit,
    ) : MapNode {
        override fun onRemoved() {
            circleManager.delete(circle)
        }

        override fun onCleared() {
            circleManager.delete(circle)
        }
    }

    internal class SymbolNode(
        val symbolManager: SymbolManager,
        val symbol: Symbol,
        var onSymbolDragged: (Symbol) -> Unit,
        var onSymbolDragStopped: (Symbol) -> Unit,
    ) : MapNode {
        override fun onRemoved() {
            symbolManager.delete(symbol)
        }

        override fun onCleared() {
            symbolManager.delete(symbol)
        }
    }

    internal class PolyLineNode(
        val lineManager: LineManager,
        val polyLine: Line,
    ) : MapNode {
        override fun onRemoved() {
            lineManager.delete(polyLine)
        }

        override fun onCleared() {
            lineManager.delete(polyLine)
        }
    }

    internal class FillNode(
        val fillManager: FillManager,
        val fill: Fill,
    ) : MapNode {
        override fun onRemoved() {
            fillManager.delete(fill)
        }

        override fun onCleared() {
            fillManager.delete(fill)
        }
    }

    private inline fun <reified NodeT : MapNode, I, O> Iterable<MapNode>.findInputCallback(
        nodeMatchPredicate: (NodeT) -> Boolean,
        nodeInputCallback: NodeT.() -> ((I) -> O)?,
    ): ((I) -> O)? {
        val callback: ((I) -> O)? = null
        for (item in this) {
            if (item is NodeT && nodeMatchPredicate(item)) {
                // Found a matching node
                return nodeInputCallback(item)
            }
        }
        return callback
    }

    override fun onClear() {
        decorations.forEach { it.onCleared() }
        decorations.clear()
        circleManagerMap.values.forEach { it.deleteAll() }
        circleManagerMap.clear()
        fillManagerMap.values.forEach { it.deleteAll() }
        fillManagerMap.clear()
        symbolManagerMap.values.forEach { it.deleteAll() }
        symbolManagerMap.clear()
        lineManagerMap.values.forEach { it.deleteAll() }
        lineManagerMap.clear()
        zIndexReferenceAnnotationManagerMap.values.forEach { it.deleteAll() }
        zIndexReferenceAnnotationManagerMap.clear()

    }

    override fun insertBottomUp(index: Int, instance: MapNode) {
        decorations.add(index, instance)
        instance.onAttached()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {
        // insertBottomUp is preferred
    }

    override fun move(from: Int, to: Int, count: Int) {
        decorations.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            decorations[index + it].onRemoved()
        }
        decorations.remove(index, count)
    }
}
