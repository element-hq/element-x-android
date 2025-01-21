/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import android.content.Context
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin

inline fun <reified N : Node> Node.createNode(
    buildContext: BuildContext,
    plugins: List<Plugin> = emptyList()
): N {
    val bindings: NodeFactoriesBindings = bindings()
    return bindings.createNode(buildContext, plugins)
}

inline fun <reified N : Node> Context.createNode(
    buildContext: BuildContext,
    plugins: List<Plugin> = emptyList()
): N {
    val bindings: NodeFactoriesBindings = bindings()
    return bindings.createNode(buildContext, plugins)
}

inline fun <reified N : Node> NodeFactoriesBindings.createNode(
    buildContext: BuildContext,
    plugins: List<Plugin> = emptyList()
): N {
    val nodeClass = N::class.java
    val nodeFactoryMap = nodeFactories()
    // Note to developers: If you got the error below, make sure to build again after
    // clearing the cache (sometimes several times) to let Dagger generate the NodeFactory.
    val nodeFactory = nodeFactoryMap[nodeClass] ?: error("Cannot find NodeFactory for ${nodeClass.name}.")

    @Suppress("UNCHECKED_CAST")
    val castedNodeFactory = nodeFactory as? AssistedNodeFactory<N>
    val node = castedNodeFactory?.create(buildContext, plugins)
    return node as N
}

interface NodeFactoriesBindings {
    fun nodeFactories(): Map<Class<out Node>, AssistedNodeFactory<*>>
}
