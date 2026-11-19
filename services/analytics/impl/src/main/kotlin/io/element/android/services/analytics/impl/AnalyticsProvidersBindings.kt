/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import io.element.android.services.analyticsproviders.api.AnalyticsProvider

/**
 * The set of [AnalyticsProvider] can be empty, for instance for an F-Droid build where only PostHog is configured,
 * since PostHog is not included in F-Droid builds.
 */
@ContributesTo(AppScope::class)
interface AnalyticsProvidersBindings {
    @Multibinds(allowEmpty = true)
    fun analyticsProviders(): Set<AnalyticsProvider>
}
