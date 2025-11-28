/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.annotations.SentrySdkDsn

@BindingContainer
@ContributesTo(AppScope::class)
object NoopAnalyticsModule {
    @SentrySdkDsn
    @Provides
    fun provideSentrySdkDsn(): String? = null
}
