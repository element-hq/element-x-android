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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList

@ContributesNode(RoomScope::class)
class MessagesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: MessagesPresenter,
) : Node(buildContext, plugins = plugins) {

    private val callback = plugins<Callback>().firstOrNull()

    interface Callback : Plugin {
        fun onRoomDetailsClicked()
        fun onEventClicked(event: TimelineItem.Event)
        fun onPreviewAttachments(attachments: ImmutableList<Attachment>)
        fun onUserDataClicked(userId: UserId)
    }

    private fun onRoomDetailsClicked() {
        callback?.onRoomDetailsClicked()
    }

    private fun onEventClicked(event: TimelineItem.Event) {
        callback?.onEventClicked(event)
    }

    private fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
        callback?.onPreviewAttachments(attachments)
    }

    private fun onUserDataClicked(userId: UserId) {
        callback?.onUserDataClicked(userId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        MessagesView(
            state = state,
            onBackPressed = this::navigateUp,
            onRoomDetailsClicked = this::onRoomDetailsClicked,
            onEventClicked = this::onEventClicked,
            onPreviewAttachments = this::onPreviewAttachments,
            onUserDataClicked = this::onUserDataClicked,
            modifier = modifier,
        )
    }
}
