/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.api

import android.content.Context
import android.content.Intent
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint

interface LockScreenEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(parentNode: Node, buildContext: BuildContext, navTarget: Target): NodeBuilder
    fun pinUnlockIntent(context: Context): Intent

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onSetupDone()
    }

    enum class Target {
        Settings,
        Setup,
    }
}
