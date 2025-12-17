/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.call.impl.receivers.DeclineCallBroadcastReceiver
import io.element.android.features.call.impl.ui.ElementCallActivity
import io.element.android.features.call.impl.ui.IncomingCallActivity

@ContributesTo(AppScope::class)
interface CallBindings {
    fun inject(callActivity: ElementCallActivity)
    fun inject(callActivity: IncomingCallActivity)
    fun inject(declineCallBroadcastReceiver: DeclineCallBroadcastReceiver)
}
