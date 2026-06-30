/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint

/**
 * An entry point for features that want to lock the screen and require
 * the user to unlock it before they can interact with the app.
 * - if the system lock is available, it will be used to unlock the screen.
 * - if the system lock is not available, but app lock is available, it will be used to unlock the screen.
 * - if neither is available, the screen will be unlocked immediately.
 *
 * The Node provided by [createNode] has to be added as a PermanentChild.
 */
interface DeviceUnlockEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
    ): Node

    fun requestUnlock(callback: Callback)

    interface Callback {
        fun onCancel()
        fun onUnlock()
    }
}
