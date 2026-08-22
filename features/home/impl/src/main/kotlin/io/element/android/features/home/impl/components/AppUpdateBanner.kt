/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.appupdate.api.AppUpdateBannerEvents
import io.element.android.features.appupdate.api.AppUpdateBannerState
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType

/**
 * Feral in-app updater banner ("mise à jour disponible"), shown at the top of the room
 * list when a newer signed APK is published on feralisme.fr. Feral-owned file.
 */
@Composable
internal fun AppUpdateBanner(
    state: AppUpdateBannerState,
    modifier: Modifier = Modifier,
) {
    val update = state.update ?: return
    val description = when (val step = state.step) {
        is AppUpdateStep.Downloading ->
            if (step.percent != null) {
                stringResource(R.string.banner_app_update_downloading_percent, step.percent ?: 0)
            } else {
                stringResource(R.string.banner_app_update_downloading)
            }
        AppUpdateStep.Failed -> stringResource(R.string.banner_app_update_failed)
        is AppUpdateStep.ReadyToInstall -> stringResource(R.string.banner_app_update_ready)
        AppUpdateStep.Idle -> stringResource(R.string.banner_app_update_message, update.versionName)
    }
    val actionText = when (state.step) {
        AppUpdateStep.Failed -> stringResource(R.string.banner_app_update_action_retry)
        is AppUpdateStep.ReadyToInstall -> stringResource(R.string.banner_app_update_action_install)
        else -> stringResource(R.string.banner_app_update_action)
    }
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        title = stringResource(R.string.banner_app_update_title),
        description = description,
        type = AnnouncementType.Actionable(
            actionText = actionText,
            onActionClick = { state.eventSink(AppUpdateBannerEvents.StartUpdate) },
            onDismissClick = { state.eventSink(AppUpdateBannerEvents.Dismiss) },
        ),
    )
}
