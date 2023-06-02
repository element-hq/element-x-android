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
import io.element.android.features.messages.impl.media.viewer.MediaViewerNode
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
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
            val title: String,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
            val mimeType: String?,
        ) : NavTarget

        @Parcelize
        data class AttachmentPreview(val attachment: Attachment) : NavTarget
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
                }
                createNode<MessagesNode>(buildContext, listOf(callback))
            }
            is NavTarget.MediaViewer -> {
                val inputs = MediaViewerNode.Inputs(
                    name = navTarget.title,
                    mediaSource = navTarget.mediaSource,
                    thumbnailSource = navTarget.thumbnailSource,
                    mimeType = navTarget.mimeType,
                )
                createNode<MediaViewerNode>(buildContext, listOf(inputs))
            }
            is NavTarget.AttachmentPreview -> {
                val inputs = AttachmentsPreviewNode.Inputs(navTarget.attachment)
                createNode<AttachmentsPreviewNode>(buildContext, listOf(inputs))
            }
        }
    }

    private fun processEventClicked(event: TimelineItem.Event) {
        when (event.content) {
            is TimelineItemImageContent -> {
                val navTarget = NavTarget.MediaViewer(
                    title = event.content.body,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.mediaSource,
                    mimeType = event.content.mimeType
                )
                backstack.push(navTarget)
            }
            is TimelineItemVideoContent -> {
                val mediaSource = event.content.videoSource
                val navTarget = NavTarget.MediaViewer(
                    title = event.content.body,
                    mediaSource = mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                    mimeType = event.content.mimeType,
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
        )
    }
}
