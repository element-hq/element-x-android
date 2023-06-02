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

package io.element.android.libraries.architecture

import android.content.Context
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin

inline fun <reified NODE : Node> Node.createNode(context: BuildContext, plugins: List<Plugin> = emptyList()): NODE {
    val bindings: NodeFactoriesBindings = bindings()
    return bindings.createNode(context, plugins)
}

inline fun <reified NODE : Node> Context.createNode(context: BuildContext, plugins: List<Plugin> = emptyList()): NODE {
    val bindings: NodeFactoriesBindings = bindings()
    return bindings.createNode(context, plugins)
}

inline fun <reified NODE : Node> NodeFactoriesBindings.createNode(context: BuildContext, plugins: List<Plugin> = emptyList()): NODE {
    val nodeClass = NODE::class.java
    val nodeFactoryMap = nodeFactories()
    // Note to developers: If you got the error below, make sure to build again after
    // clearing the cache (sometimes several times) to let Dagger generate the NodeFactory.
    val nodeFactory = nodeFactoryMap[nodeClass] ?: error("Cannot find NodeFactory for ${nodeClass.name}.")

    @Suppress("UNCHECKED_CAST")
    val castedNodeFactory = nodeFactory as? AssistedNodeFactory<NODE>
    val node = castedNodeFactory?.create(context, plugins)
    return node as NODE
}

interface NodeFactoriesBindings {
    fun nodeFactories(): Map<Class<out Node>, AssistedNodeFactory<*>>
}
