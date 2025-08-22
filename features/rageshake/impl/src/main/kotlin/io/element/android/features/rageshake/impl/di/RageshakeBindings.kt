/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.features.rageshake.impl.crash.PreferencesCrashDataStore
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
interface RageshakeBindings {
    fun preferencesCrashDataStore(): PreferencesCrashDataStore
}
