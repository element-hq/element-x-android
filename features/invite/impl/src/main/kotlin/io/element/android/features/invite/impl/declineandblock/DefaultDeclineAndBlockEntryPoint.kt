/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.acceptdecline.InviteData
import io.element.android.features.invite.api.declineandblock.DeclineAndBlockEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope

@ContributesBinding(AppScope::class)
class DefaultDeclineAndBlockEntryPoint : DeclineAndBlockEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext, inviteData: InviteData): Node {
        val inputs = DeclineAndBlockNode.Inputs(inviteData)
        return parentNode.createNode<DeclineAndBlockNode>(buildContext, plugins = listOf(inputs))
    }
}
