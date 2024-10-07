/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
        fun onStartCall(dmRoomId: RoomId)
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
