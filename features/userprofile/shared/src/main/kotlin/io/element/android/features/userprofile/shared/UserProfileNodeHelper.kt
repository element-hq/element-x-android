/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.userprofile.shared

import android.content.Context
import io.element.android.libraries.androidutils.R
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

class UserProfileNodeHelper(
    private val userId: UserId,
) {
    interface Callback : NodeInputs {
        fun openAvatarPreview(username: String, avatarUrl: String)
        fun onStartDM(roomId: RoomId)
    }

    fun onShareUser(
        context: Context,
        permalinkBuilder: PermalinkBuilder,
    ) {
        val permalinkResult = permalinkBuilder.permalinkForUser(userId)
        permalinkResult.onSuccess { permalink ->
            context.startSharePlainTextIntent(
                activityResultLauncher = null,
                chooserTitle = context.getString(CommonStrings.action_share),
                text = permalink,
                noActivityFoundMessage = context.getString(R.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }
}
