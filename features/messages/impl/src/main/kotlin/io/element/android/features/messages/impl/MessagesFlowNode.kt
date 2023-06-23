/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewNode
import io.element.android.features.messages.impl.forward.ForwardMessagesNode
import io.element.android.features.messages.impl.media.local.MediaInfo
import io.element.android.features.messages.impl.media.viewer.MediaViewerNode
import io.element.android.features.messages.impl.report.ReportMessageNode
import io.element.android.features.messages.impl.timeline.debug.EventDebugInfoNode
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class MessagesFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BackstackNode<MessagesFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Messages,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Messages : NavTarget

        @Parcelize
        data class MediaViewer(
            val mediaInfo: MediaInfo,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
        ) : NavTarget

        @Parcelize
        data class AttachmentPreview(val attachment: Attachment) : NavTarget

        @Parcelize
        data class EventDebugInfo(val eventId: EventId, val debugInfo: TimelineItemDebugInfo) : NavTarget

        @Parcelize
        data class ForwardEvent(val eventId: EventId) : NavTarget

        @Parcelize
        data class ReportMessage(val eventId: EventId, val senderId: UserId) : NavTarget
    }

    private val callback = plugins<MessagesEntryPoint.Callback>().firstOrNull()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Messages -> {
                val callback = object : MessagesNode.Callback {
                    override fun onRoomDetailsClicked() {
                        callback?.onRoomDetailsClicked()
                    }

                    override fun onEventClicked(event: TimelineItem.Event) {
                        processEventClicked(event)
                    }

                    override fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
                        backstack.push(NavTarget.AttachmentPreview(attachments.first()))
                    }

                    override fun onUserDataClicked(userId: UserId) {
                        callback?.onUserDataClicked(userId)
                    }

                    override fun onShowEventDebugInfoClicked(eventId: EventId, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun onForwardEventClicked(eventId: EventId) {
                        backstack.push(NavTarget.ForwardEvent(eventId))
                    }

                    override fun onReportMessage(eventId: EventId, senderId: UserId) {
                        backstack.push(NavTarget.ReportMessage(eventId, senderId))
                    }
                }
                createNode<MessagesNode>(buildContext, listOf(callback))
            }
            is NavTarget.MediaViewer -> {
                val inputs = MediaViewerNode.Inputs(
                    mediaInfo = navTarget.mediaInfo,
                    mediaSource = navTarget.mediaSource,
                    thumbnailSource = navTarget.thumbnailSource,
                )
                createNode<MediaViewerNode>(buildContext, listOf(inputs))
            }
            is NavTarget.AttachmentPreview -> {
                val inputs = AttachmentsPreviewNode.Inputs(navTarget.attachment)
                createNode<AttachmentsPreviewNode>(buildContext, listOf(inputs))
            }
            is NavTarget.EventDebugInfo -> {
                val inputs = EventDebugInfoNode.Inputs(navTarget.eventId, navTarget.debugInfo)
                createNode<EventDebugInfoNode>(buildContext, listOf(inputs))
            }
            is NavTarget.ForwardEvent -> {
                val inputs = ForwardMessagesNode.Inputs(navTarget.eventId)
                val callback = object : ForwardMessagesNode.Callback {
                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        this@MessagesFlowNode.callback?.onForwardedToSingleRoom(roomId)
                    }
                }
                createNode<ForwardMessagesNode>(buildContext, listOf(inputs, callback))
            }
            is NavTarget.ReportMessage -> {
                val inputs = ReportMessageNode.Inputs(navTarget.eventId, navTarget.senderId)
                createNode<ReportMessageNode>(buildContext, listOf(inputs))
            }
        }
    }

    private fun processEventClicked(event: TimelineItem.Event) {
        when (event.content) {
            is TimelineItemImageContent -> {
                val navTarget = NavTarget.MediaViewer(
                    mediaInfo = MediaInfo(
                        name = event.content.body,
                        mimeType = event.content.mimeType,
                        formattedFileSize = event.content.formattedFileSize,
                        fileExtension = event.content.fileExtension
                    ),
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
                backstack.push(navTarget)
            }
            is TimelineItemVideoContent -> {
                val mediaSource = event.content.videoSource
                val navTarget = NavTarget.MediaViewer(
                    mediaInfo = MediaInfo(
                        name = event.content.body,
                        mimeType = event.content.mimeType,
                        formattedFileSize = event.content.formattedFileSize,
                        fileExtension = event.content.fileExtension
                    ),
                    mediaSource = mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
                backstack.push(navTarget)
            }
            is TimelineItemFileContent -> {
                val mediaSource = event.content.fileSource
                val navTarget = NavTarget.MediaViewer(
                    mediaInfo = MediaInfo(
                        name = event.content.body,
                        mimeType = event.content.mimeType,
                        formattedFileSize = event.content.formattedFileSize,
                        fileExtension = event.content.fileExtension
                    ),
                    mediaSource = mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
                backstack.push(navTarget)
            }
            else -> Unit
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
