/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import kotlinx.parcelize.Parcelize

interface SecureBackupEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data object Root : InitialTarget

        @Parcelize
        data object SetUpRecovery : InitialTarget

        @Parcelize
        data object EnterRecoveryKey : InitialTarget

        @Parcelize
        data object ResetIdentity : InitialTarget
    }

    data class Params(val initialElement: InitialTarget) : NodeInputs

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface Callback : Plugin {
        fun onDone()
    }

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }
}
