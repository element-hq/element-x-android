/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs

/**
 * Entry point for the recovery-key setup screen.
 *
 * The default implementation builds the standard auto-generated-key setup node. The binding can be
 * replaced to provide an alternative setup node.
 */
interface SecureBackupSetupEntryPoint : FeatureEntryPoint {
    data class Inputs(val isChangeRecoveryKeyUserStory: Boolean) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: Inputs,
    ): Node
}
