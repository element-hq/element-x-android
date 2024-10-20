/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint

interface MapRealtimeEntryPoint : FeatureEntryPoint {

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    fun createNode(parentNode: Node, buildContext: BuildContext): Node

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        // Add your callbacks
    }
}
