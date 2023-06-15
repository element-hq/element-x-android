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

package io.element.android.features.createroom.impl.root

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
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.ui.strings.R
import timber.log.Timber

@ContributesNode(SessionScope::class)
class CreateRoomRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: CreateRoomRootPresenter,
    private val matrixClient: MatrixClient,
    private val buildMeta: BuildMeta,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onCreateNewRoom()
        fun onStartChatSuccess(roomId: RoomId)
    }

    private val callback = object : Callback {
        override fun onCreateNewRoom() {
            plugins<Callback>().forEach { it.onCreateNewRoom() }
        }

        override fun onStartChatSuccess(roomId: RoomId) {
            plugins<Callback>().forEach { it.onStartChatSuccess(roomId) }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        CreateRoomRootView(
            state = state,
            modifier = modifier,
            onClosePressed = this::navigateUp,
            onNewRoomClicked = callback::onCreateNewRoom,
            onOpenDM = callback::onStartChatSuccess,
            onInviteFriendsClicked = { invitePeople(context) }
        )
    }

    private fun invitePeople(context: Context) {
        val permalinkResult = PermalinkBuilder.permalinkForUser(matrixClient.sessionId)
        permalinkResult.onSuccess { permalink ->
            val appName = buildMeta.applicationName
            startSharePlainTextIntent(
                context = context,
                activityResultLauncher = null,
                chooserTitle = context.getString(R.string.action_invite_friends_to_app),
                text = context.getString(R.string.invite_friends_text, appName, permalink),
                extraTitle = context.getString(R.string.invite_friends_rich_title, appName),
                noActivityFoundMessage = context.getString(io.element.android.libraries.androidutils.R.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }
}
