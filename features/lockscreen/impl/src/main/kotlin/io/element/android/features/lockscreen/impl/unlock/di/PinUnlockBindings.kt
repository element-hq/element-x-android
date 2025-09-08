/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.lockscreen.impl.unlock.activity.PinUnlockActivity

@ContributesTo(AppScope::class)
interface PinUnlockBindings {
    fun inject(activity: PinUnlockActivity)
}
