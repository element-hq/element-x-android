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
import io.element.android.features.securebackup.api.SecureBackupRecoveryKeyEntryPoint
import io.element.android.features.securebackup.api.SecureBackupSetupEntryPoint
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.ui.strings.CommonStrings

@ContributesBinding(AppScope::class)
class DefaultSecureBackupSetupEntryPoint : SecureBackupSetupEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: SecureBackupSetupEntryPoint.Inputs,
    ): Node {
        lateinit var node: Node
        node = parentNode.createNode<SecureBackupGenerateKeyNode>(
            buildContext = buildContext,
            plugins = listOf(
                SecureBackupGenerateKeyNode.Inputs(
                    isReplacingExistingKey = inputs.isChangeRecoveryKeyUserStory,
                    doneButtonText = CommonStrings.action_done,
                ),
                object : SecureBackupRecoveryKeyEntryPoint.Callback {
                    override fun onDone() = node.navigateUp()
                },
            ),
        )
        return node
    }
}
