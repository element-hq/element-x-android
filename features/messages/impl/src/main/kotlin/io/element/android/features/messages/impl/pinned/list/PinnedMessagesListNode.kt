/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.list

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.impl.MessagesNode.Callback
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser

@ContributesNode(RoomScope::class)
class PinnedMessagesListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PinnedMessagesListPresenter,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val permalinkParser: PermalinkParser,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onEventClick(event: TimelineItem.Event)
        fun onUserDataClick(userId: UserId)
        fun onPermalinkClick(data: PermalinkData)
    }

    private val callbacks = plugins<Callback>()

    private fun onEventClick(event: TimelineItem.Event) {
        return callbacks.forEach { it.onEventClick(event) }
    }

    private fun onUserDataClick(userId: UserId) {
        callbacks.forEach { it.onUserDataClick(userId) }
    }

    private fun onLinkClick(context: Context, url: String) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callbacks.forEach { it.onUserDataClick(permalink.userId) }
            }
            is PermalinkData.RoomLink -> {
                callbacks.forEach { it.onPermalinkClick(permalink) }
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val context = LocalContext.current
            val state = presenter.present()
            PinnedMessagesListView(
                state = state,
                onBackClick = ::navigateUp,
                onEventClick = ::onEventClick,
                onUserDataClick = ::onUserDataClick,
                onLinkClick = { url -> onLinkClick(context, url) },
                modifier = modifier
            )
        }
    }
}
