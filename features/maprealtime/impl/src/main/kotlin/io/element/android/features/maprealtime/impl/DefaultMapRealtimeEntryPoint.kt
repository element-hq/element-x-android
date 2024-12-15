/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.maprealtime.api.MapRealtimeEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultMapRealtimeEntryPoint @Inject constructor() : MapRealtimeEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MapRealtimeEntryPoint.NodeBuilder {
        TODO("Not yet implemented")
    }

    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        return parentNode.createNode<MapRealtimePresenterNode>(buildContext)
    }
}
