package io.element.android.features.${MODULE_NAME}.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.${MODULE_NAME}.api.${FEATURE_NAME}EntryPoint
import io.element.android.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class Default${FEATURE_NAME}EntryPoint() : ${FEATURE_NAME}EntryPoint {

    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): ${FEATURE_NAME}EntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : ${FEATURE_NAME}EntryPoint.NodeBuilder {

            override fun callback(callback: ${FEATURE_NAME}EntryPoint.Callback): ${FEATURE_NAME}EntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<${FEATURE_NAME}FlowNode>(buildContext, plugins)
            }
        }
    }
}
