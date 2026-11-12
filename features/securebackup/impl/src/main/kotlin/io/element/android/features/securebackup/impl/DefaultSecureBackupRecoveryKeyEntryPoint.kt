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
import io.element.android.features.securebackup.impl.check.SecureBackupCheckKeyNode
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyNode
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultSecureBackupRecoveryKeyEntryPoint : SecureBackupRecoveryKeyEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: SecureBackupRecoveryKeyEntryPoint.Inputs,
        callback: SecureBackupRecoveryKeyEntryPoint.Callback,
    ): Node {
        return when (val mode = inputs.mode) {
            is SecureBackupRecoveryKeyEntryPoint.Mode.Generate -> parentNode.createNode<SecureBackupGenerateKeyNode>(
                buildContext = buildContext,
                plugins = listOf(
                    SecureBackupGenerateKeyNode.Inputs(
                        isReplacingExistingKey = mode.isReplacingExistingKey,
                        doneButtonText = inputs.doneButtonText,
                    ),
                    callback,
                ),
            )
            SecureBackupRecoveryKeyEntryPoint.Mode.Check -> parentNode.createNode<SecureBackupCheckKeyNode>(
                buildContext = buildContext,
                plugins = listOf(
                    SecureBackupCheckKeyNode.Inputs(doneButtonText = inputs.doneButtonText),
                    callback,
                ),
            )
        }
    }
}
