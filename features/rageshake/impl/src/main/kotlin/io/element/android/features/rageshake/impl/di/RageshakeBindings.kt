/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.rageshake.impl.crash.PreferencesCrashDataStore

@ContributesTo(AppScope::class)
interface RageshakeBindings {
    fun preferencesCrashDataStore(): PreferencesCrashDataStore
}
