/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.threads.ThreadListItemData

@ContributesNode(RoomScope::class)
@AssistedInject
class ThreadsListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: JoinedRoom,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onThreadClick(eventId: EventId)
    }

    private val callback: Callback = callback()

    private var threads by mutableStateOf<List<ThreadListItemData>>(emptyList())
    private var isLoading by mutableStateOf(true)

    @Composable
    override fun View(modifier: Modifier) {
        LaunchedEffect(Unit) {
            isLoading = true
            room.loadThreadList()
                .onSuccess { threads = it }
                .onFailure { threads = emptyList() }
            isLoading = false
        }

        ThreadsListView(
            threads = threads,
            isLoading = isLoading,
            onThreadClick = callback::onThreadClick,
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }
}
