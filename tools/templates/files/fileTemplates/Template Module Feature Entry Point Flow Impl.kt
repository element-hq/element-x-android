package io.element.android.features.${MODULE_NAME}.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.${MODULE_NAME}.api.${FEATURE_NAME}EntryPoint
import io.element.android.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope

@ContributesBinding(AppScope::class)
class Default${FEATURE_NAME}EntryPoint() : ${FEATURE_NAME}EntryPoint {

    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: ${FEATURE_NAME}EntryPoint.Callback,
    ): Node {
        return parentNode.createNode<${FEATURE_NAME}FlowNode>(buildContext, listOf(callback))
    }
}
