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

package io.element.android.features.roomdetails.impl

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.MatrixRoom

@ContributesNode(RoomScope::class)
class RoomDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomDetailsPresenter,
    private val room: MatrixRoom,
) : Node(buildContext, plugins = plugins) {

    private val callback = plugins<RoomDetailsFlowNode.Callback>().firstOrNull()

    private fun openRoomMemberList() {
        callback?.openRoomMemberList()
    }

    private fun onShareRoom(context: Context) {
        val alias = room.alias ?: room.alternativeAliases.firstOrNull()
        val permalinkResult = alias?.let { PermalinkBuilder.permalinkForRoomAlias(it) }
            ?: PermalinkBuilder.permalinkForRoomId(room.roomId)
        permalinkResult.onSuccess { permalink ->
            startSharePlainTextIntent(
                context = context,
                activityResultLauncher = null,
                chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                text = permalink,
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state = presenter.present()
        RoomDetailsView(
            state = state,
            modifier = modifier,
            goBack = { navigateUp() },
            onShareRoom = { onShareRoom(context) },
            openRoomMemberList = ::openRoomMemberList,
        )
    }
}
