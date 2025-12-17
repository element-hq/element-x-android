/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.notifications

import io.element.android.libraries.permissions.api.PermissionsState

data class NotificationsOptInState(
    val notificationsPermissionState: PermissionsState,
    val eventSink: (NotificationsOptInEvents) -> Unit
)
