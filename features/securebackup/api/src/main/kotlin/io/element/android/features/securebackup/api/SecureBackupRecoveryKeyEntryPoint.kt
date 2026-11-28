/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs

/**
 * Entry point of the screens to generate a new recovery key or to check the existing one, for flows that need
 * one of them in the middle of something else, such as removing the device.
 */
interface SecureBackupRecoveryKeyEntryPoint : FeatureEntryPoint {
    sealed interface Mode {
        /**
         * Generate a new recovery key. It only becomes active once the user confirms that they have saved it.
         *
         * @param isReplacingExistingKey whether the user already has a recovery key that the new one replaces.
         */
        data class Generate(val isReplacingExistingKey: Boolean) : Mode

        /** Make sure that the recovery key the user already has is correct. */
        data object Check : Mode
    }

    /**
     * @param mode what the user is asked to do.
     * @param doneButtonText the string resource of the button that finishes the screens, as it depends on the calling flow.
     */
    data class Inputs(
        val mode: Mode,
        val doneButtonText: Int,
    ) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: Inputs,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        /** The user has finished and wants to carry on with the calling flow. */
        fun onDone()
    }
}
