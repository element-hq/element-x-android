package io.element.android.libraries.architecture.appyx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bumble.appyx.core.integration.NodeFactory
import com.bumble.appyx.core.integrationpoint.IntegrationPoint
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.NavElement
import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.build
import com.bumble.appyx.core.state.SavedStateMap
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectory
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import timber.log.Timber

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

/**
 * Contains the last captured navigation state in a human-readable format, which can be attached to crash reports to help
 * with debugging `TransactionTooLargeException` crashes.
 */
var lastCapturedNavState: String = "No nav state captured yet"

private data class NodeEntry(
    val navKey: Any?,
    val children: List<NodeEntry> = emptyList()
) {
    override fun toString(): String {
        val key = navKey ?: return ""
        return buildString {
            append(key.javaClass.name)
            if (children.isNotEmpty()) {
                append("=[")
                append(children.joinToString(", "))
                append("]")
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.buildNavStateMap(): List<NodeEntry> {
    val children = this["ChildrenState"] as? Map<NavKey<*>, Map<String, Any?>> ?: return emptyList()
    return children.entries.map { (key, value) ->
        NodeEntry(
            navKey = key.navTarget,
            children = value.buildNavStateMap()
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.buildNavModel(name: String): List<NodeEntry> {
    val navModel = this[name] as? List<NavElement<*, *>> ?: return emptyList()
    return navModel.map {
        NodeEntry(
            navKey = it.key.navTarget,
            children = emptyList()
        )
    }
}

// Once we have fixed the `TransactionTooLargeException` issues, we should remove this and use the default `NodeHost` implementation
@Suppress("ComposableParamOrder") // detekt complains as 'factory' param isn't a pure lambda
@Composable
fun <N : Node> DebugNavStateNodeHost(
    integrationPoint: IntegrationPoint,
    modifier: Modifier = Modifier,
    customisations: NodeCustomisationDirectory = remember { NodeCustomisationDirectoryImpl() },
    factory: NodeFactory<N>
) {
    val node by rememberNode(factory, "AppyxMainNode", customisations, integrationPoint)
    DisposableEffect(node) {
        onDispose { node.updateLifecycleState(Lifecycle.State.DESTROYED) }
    }
    node.Compose(modifier = modifier)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        node.updateLifecycleState(lifecycle.currentState)
        val observer = LifecycleEventObserver { source, _ ->
            node.updateLifecycleState(source.lifecycle.currentState)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun <N : Node> rememberNode(
    factory: NodeFactory<N>,
    key: String,
    customisations: NodeCustomisationDirectory,
    integrationPoint: IntegrationPoint,
): State<N> {
    fun createNode(savedStateMap: SavedStateMap?): N =
        factory
            .create(
                buildContext = BuildContext.root(
                    savedStateMap = savedStateMap,
                    customisations = customisations
                ),
            )
            .apply { this.integrationPoint = integrationPoint }
            .build()

    // This is deprecated because using the custom key would not make this unique, but we work around that by using the currentCompositeKeyHashCode
    // as part of the key, which should be unique for each call site of rememberNode.
    @Suppress("DEPRECATION")
    return rememberSaveable(
        inputs = arrayOf(),
        key = "$key:$currentCompositeKeyHashCode",
        stateSaver = mapSaver(
            save = { node ->
                val result = node.saveInstanceState(this)
                // We want to capture the nav state in a format that's easier to read and understand in crash reports, so we build a custom map for that.
                val copy = result.toMutableMap()
                copy["ChildrenState"] = copy.buildNavStateMap()
                val navModelKey = "NavModel"
                if (copy.contains(navModelKey)) {
                    copy[navModelKey] = copy.buildNavModel(navModelKey)
                }
                val permanentNavModelKey = "com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel"
                if (copy.contains(permanentNavModelKey)) {
                    copy[permanentNavModelKey] =
                        copy.buildNavModel(permanentNavModelKey)
                }
                Timber.d("Saving nav state: $copy")
                // Store the last nav state in a global variable so that it can be attached to crash reports if the app crashes before the next save happens.
                lastCapturedNavState = copy.toString()
                result
            },
            restore = { state -> createNode(savedStateMap = state) },
        ),
    ) {
        mutableStateOf(createNode(savedStateMap = null))
    }
}
