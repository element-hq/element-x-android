/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.poll.api.history.PollHistoryEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPollHistoryEntryPoint @Inject constructor() : PollHistoryEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        return parentNode.createNode<PollHistoryFlowNode>(buildContext)
    }
}
