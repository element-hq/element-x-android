/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messageretention.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messageretention.api.MessageRetentionEntryPoint
import io.element.android.libraries.di.RoomScope

/**
 * No-op FOSS binding. Message retention is an Element Pro feature, so the FOSS build never surfaces
 * the entry point — this returns an inert node and enterprise builds replace this binding with the
 * real flow.
 */
@ContributesBinding(RoomScope::class)
class DefaultMessageRetentionEntryPoint : MessageRetentionEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: MessageRetentionEntryPoint.Callback,
    ): Node = object : Node(buildContext, plugins = listOf(callback)) {
        @Composable
        override fun View(modifier: Modifier) = Unit
    }
}
