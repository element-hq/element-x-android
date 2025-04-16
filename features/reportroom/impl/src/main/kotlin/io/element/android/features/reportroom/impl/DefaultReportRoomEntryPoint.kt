/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultReportRoomEntryPoint @Inject constructor() : ReportRoomEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext, roomId: RoomId): Node {
        return parentNode.createNode<ReportRoomNode>(buildContext, plugins = listOf(ReportRoomNode.Inputs(roomId)))
    }
}
