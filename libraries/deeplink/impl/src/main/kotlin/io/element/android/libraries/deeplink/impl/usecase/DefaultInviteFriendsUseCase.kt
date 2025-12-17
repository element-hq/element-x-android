/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl.usecase

import android.app.Activity
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.deeplink.api.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import io.element.android.libraries.androidutils.R as AndroidUtilsR

@ContributesBinding(SessionScope::class)
class DefaultInviteFriendsUseCase(
    private val stringProvider: StringProvider,
    private val matrixClient: MatrixClient,
    private val buildMeta: BuildMeta,
    private val permalinkBuilder: PermalinkBuilder,
) : InviteFriendsUseCase {
    override fun execute(activity: Activity) {
        val permalinkResult = permalinkBuilder.permalinkForUser(matrixClient.sessionId)
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
