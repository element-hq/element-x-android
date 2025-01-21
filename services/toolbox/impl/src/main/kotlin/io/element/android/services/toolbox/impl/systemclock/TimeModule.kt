/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.systemclock

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import kotlin.time.TimeSource

@Module
@ContributesTo(AppScope::class)
object TimeModule {
    @Provides
    fun timeSource(): TimeSource {
        return TimeSource.Monotonic
    }
}
