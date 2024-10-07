/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
interface TestNotificationReceiverBinding {
    fun inject(service: TestNotificationReceiver)
}
