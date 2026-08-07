/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.ThreadId

@ContributesNode(RoomScope::class)
@AssistedInject
class ThreadsListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: ThreadsListPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openThread(threadId: ThreadId)
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        ThreadsListView(
            state = presenter.present(),
            modifier = modifier,
            onThreadClick = callback::openThread,
            onBackClick = this::navigateUp,
        )
    }
}
