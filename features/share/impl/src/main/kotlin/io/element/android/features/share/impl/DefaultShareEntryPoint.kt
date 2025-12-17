/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultShareEntryPoint : ShareEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: ShareEntryPoint.Params,
        callback: ShareEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<ShareNode>(
            buildContext = buildContext,
            plugins = listOf(
                ShareNode.Inputs(intent = params.intent),
                callback,
            )
        )
    }
}
