/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import io.element.android.features.callnative.api.analytics.CallAnalyticsClientFactory

/**
 * Declares the set of [CallAnalyticsClientFactory] as possibly empty: the PostHog one is only in gplay builds.
 */
@ContributesTo(AppScope::class)
interface CallAnalyticsBindings {
    @Multibinds(allowEmpty = true)
    fun callAnalyticsClientFactories(): Set<CallAnalyticsClientFactory>
}
