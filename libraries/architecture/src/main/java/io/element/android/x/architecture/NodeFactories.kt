package io.element.android.x.architecture

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin

inline fun <reified NODE : Node> Node.createNode(context: BuildContext, plugins: List<Plugin> = emptyList()): NODE {
    val nodeClass = NODE::class.java
    val bindings: NodeFactoriesBindings = bindings()
    val nodeFactoryMap = bindings.nodeFactories()
    val nodeFactory = nodeFactoryMap[nodeClass] ?: error("Cannot find NodeFactory for ${nodeClass.name}.")

    @Suppress("UNCHECKED_CAST")
    val castedNodeFactory = nodeFactory as? AssistedNodeFactory<NODE>
    val node = castedNodeFactory?.create(context, plugins)
    return node as NODE
}

interface NodeFactoriesBindings {
    fun nodeFactories(): Map<Class<out Node>, AssistedNodeFactory<*>>
}
