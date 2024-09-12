/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.ftue.impl.notifications

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.permissions.api.aPermissionsState

open class NotificationsOptInStateProvider : PreviewParameterProvider<NotificationsOptInState> {
    override val values: Sequence<NotificationsOptInState>
        get() = sequenceOf(
            aNotificationsOptInState(),
            // Add other states here
        )
}

fun aNotificationsOptInState() = NotificationsOptInState(
    notificationsPermissionState = aPermissionsState(showDialog = false),
    eventSink = {}
)
