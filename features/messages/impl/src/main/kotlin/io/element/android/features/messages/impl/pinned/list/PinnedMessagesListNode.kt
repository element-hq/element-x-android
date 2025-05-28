/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings

@ContributesNode(RoomScope::class)
class PinnedMessagesListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: PinnedMessagesListPresenter.Factory,
    actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val permalinkParser: PermalinkParser,
) : Node(buildContext, plugins = plugins), PinnedMessagesListNavigator {
    interface Callback : Plugin {
        fun onEventClick(event: TimelineItem.Event)
        fun onUserDataClick(userId: UserId)
        fun onViewInTimelineClick(eventId: EventId)
        fun onRoomPermalinkClick(data: PermalinkData.RoomLink)
        fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun onForwardEventClick(eventId: EventId)
    }

    private val presenter = presenterFactory.create(
        navigator = this,
        actionListPresenter = actionListPresenterFactory.create(PinnedMessagesListTimelineActionPostProcessor())
    )
    private val callbacks = plugins<Callback>()

    private fun onEventClick(event: TimelineItem.Event) {
        return callbacks.forEach { it.onEventClick(event) }
    }

    private fun onUserDataClick(user: MatrixUser) {
        callbacks.forEach { it.onUserDataClick(user.userId) }
    }

    private fun onLinkClick(context: Context, url: String) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callbacks.forEach { it.onUserDataClick(permalink.userId) }
            }
            is PermalinkData.RoomLink -> {
                callbacks.forEach { it.onRoomPermalinkClick(permalink) }
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    override fun onViewInTimelineClick(eventId: EventId) {
        callbacks.forEach { it.onViewInTimelineClick(eventId) }
    }

    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callbacks.forEach { it.onShowEventDebugInfoClick(eventId, debugInfo) }
    }

    override fun onForwardEventClick(eventId: EventId) {
        callbacks.forEach { it.onForwardEventClick(eventId) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val context = LocalContext.current
            val view = LocalView.current
            val state = presenter.present()
            PinnedMessagesListView(
                state = state,
                onBackClick = ::navigateUp,
                onEventClick = ::onEventClick,
                onUserDataClick = ::onUserDataClick,
                onLinkClick = { link -> onLinkClick(context, link.url) },
                onLinkLongClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS
                    )
                    context.copyToClipboard(
                        it.url,
                        context.getString(CommonStrings.common_copied_to_clipboard)
                    )
                },
                modifier = modifier
            )
        }
    }
}
