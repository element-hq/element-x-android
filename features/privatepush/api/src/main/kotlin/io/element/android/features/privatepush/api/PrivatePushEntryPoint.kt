/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint

/** Multi-page "Private notifications" setup flow (install + configure ntfy, connect, verify). */
interface PrivatePushEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        /** Setup finished and verified private. */
        fun onDone()

        /** Member tapped "Later" (already persisted as dismissed for this session). */
        fun onLater()

        /** Raised by the embedded upstream troubleshoot screen; hosts without a blocked-users screen can ignore it. */
        fun navigateToBlockedUsers() = Unit
    }
}
