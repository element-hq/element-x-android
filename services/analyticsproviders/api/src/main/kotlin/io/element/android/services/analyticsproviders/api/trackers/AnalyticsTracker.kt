/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api.trackers

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.Interaction
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties

interface AnalyticsTracker {
    /**
     * Capture an Event.
     */
    fun capture(event: VectorAnalyticsEvent)

    /**
     * Track a displayed screen.
     */
    fun screen(screen: VectorAnalyticsScreen)

    /**
     * Update user specific properties.
     */
    fun updateUserProperties(userProperties: UserProperties)

    /**
     * Update the super properties.
     * Super properties are added to any tracked event automatically.
     */
    fun updateSuperProperties(updatedProperties: SuperProperties)
}

fun AnalyticsTracker.captureInteraction(name: Interaction.Name, type: Interaction.InteractionType? = null) {
    capture(Interaction(interactionType = type, name = name))
}
