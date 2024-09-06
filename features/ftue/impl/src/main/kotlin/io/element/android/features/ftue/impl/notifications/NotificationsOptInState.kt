/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.ftue.impl.notifications

import io.element.android.libraries.permissions.api.PermissionsState

data class NotificationsOptInState(
    val notificationsPermissionState: PermissionsState,
    val eventSink: (NotificationsOptInEvents) -> Unit
)
