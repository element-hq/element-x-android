/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultSecureBackupEntryPoint : SecureBackupEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: SecureBackupEntryPoint.Params,
        callback: SecureBackupEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<SecureBackupFlowNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback)
        )
    }
}
