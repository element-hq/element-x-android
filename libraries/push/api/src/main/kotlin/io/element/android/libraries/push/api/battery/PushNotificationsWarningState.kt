/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.battery

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig

data class PushNotificationsWarningState(
    val currentUserPushConfig: AsyncData<CurrentUserPushConfig?>,
    val needsEnablingBatteryOptimization: Boolean,
    val eventSink: (PushNotificationsWarningEvents) -> Unit,
)
