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

package io.element.android.libraries.deeplink.usecase

import android.app.Activity
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject
import io.element.android.libraries.androidutils.R as AndroidUtilsR

class InviteFriendsUseCase @Inject constructor(
    private val stringProvider: StringProvider,
    private val matrixClient: MatrixClient,
    private val buildMeta: BuildMeta,
) {
    fun execute(activity: Activity) {
        val permalinkResult = PermalinkBuilder.permalinkForUser(matrixClient.sessionId)
        permalinkResult.fold(
            onSuccess = { permalink ->
                val appName = buildMeta.applicationName
                activity.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = stringProvider.getString(CommonStrings.action_invite_friends),
                    text = stringProvider.getString(CommonStrings.invite_friends_text, appName, permalink),
                    extraTitle = stringProvider.getString(CommonStrings.invite_friends_rich_title, appName),
                    noActivityFoundMessage = stringProvider.getString(AndroidUtilsR.string.error_no_compatible_app_found)
                )
            },
            onFailure = {
                Timber.e(it)
            }
        )
    }
}
