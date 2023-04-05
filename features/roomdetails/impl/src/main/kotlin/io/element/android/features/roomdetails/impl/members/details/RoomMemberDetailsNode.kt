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

package io.element.android.features.roomdetails.impl.members.details

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.RoomMember
import timber.log.Timber

@ContributesNode(RoomScope::class)
class RoomMemberDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomMemberDetailsPresenter.Factory,
) : Node(buildContext, plugins = plugins) {

    data class Inputs(
        val member: RoomMember,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val presenter = presenterFactory.create(inputs.member)

    private fun onShareUser(context: Context) {
        val permalinkResult = PermalinkBuilder.permalinkForUser(UserId(inputs.member.userId))
        permalinkResult.onSuccess { permalink ->
            startSharePlainTextIntent(
                context = context,
                activityResultLauncher = null,
                chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                text = permalink,
                noActivityFoundMessage = context.getString(io.element.android.libraries.ui.strings.R.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state = presenter.present()
        RoomMemberDetailsView(
            state = state,
            modifier = modifier,
            goBack = { navigateUp() },
            onShareUser = { onShareUser(context) }
        )
    }
}
