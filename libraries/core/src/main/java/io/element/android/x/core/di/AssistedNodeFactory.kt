package io.element.android.x.core.di

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin

interface AssistedNodeFactory<NODE : Node> {
    fun create(buildContext: BuildContext, plugins: List<Plugin>): NODE
}
