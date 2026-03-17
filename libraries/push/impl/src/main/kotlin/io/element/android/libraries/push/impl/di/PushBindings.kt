/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.libraries.push.impl.push.FetchPushForegroundService

@ContributesTo(AppScope::class)
interface PushBindings {
    fun inject(fetchPushForegroundService: FetchPushForegroundService)
}
