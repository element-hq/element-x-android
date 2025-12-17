/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.annotations.SentrySdkDsn
import io.element.android.services.analyticsproviders.sentry.SentryConfig

@BindingContainer
@ContributesTo(AppScope::class)
object SentryModule {
    @Provides
    fun provideSentrySdkDsn(): SentrySdkDsn? = SentrySdkDsn(SentryConfig.SDK_DSN)
}
