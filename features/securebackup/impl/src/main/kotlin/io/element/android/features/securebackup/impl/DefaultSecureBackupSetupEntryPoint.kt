/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.securebackup.api.SecureBackupSetupEntryPoint
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupNode
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultSecureBackupSetupEntryPoint : SecureBackupSetupEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: SecureBackupSetupEntryPoint.Inputs,
    ): Node {
        return parentNode.createNode<SecureBackupSetupNode>(
            buildContext = buildContext,
            plugins = listOf(
                SecureBackupSetupNode.Inputs(
                    isChangeRecoveryKeyUserStory = inputs.isChangeRecoveryKeyUserStory,
                )
            ),
        )
    }
}
