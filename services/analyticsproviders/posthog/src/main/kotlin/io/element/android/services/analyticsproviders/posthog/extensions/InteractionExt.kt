/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog.extensions

import im.vector.app.features.analytics.plan.Interaction

fun Interaction.Name.toAnalyticsInteraction(interactionType: Interaction.InteractionType = Interaction.InteractionType.Touch) =
    Interaction(
        name = this,
        interactionType = interactionType
    )
