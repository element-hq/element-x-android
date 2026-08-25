/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.api

import kotlinx.coroutines.flow.FlowCollector

/**
 * Carries the OAuth redirect from the activity that receives it to the login screen waiting for it.
 *
 * The two are not in the same navigation subtree, so the result cannot simply be returned.
 */
interface OAuthActionFlow {
    /**
     * Publishes the action just received, to be picked up by whoever is collecting.
     *
     * @param oAuthAction the action resolved from the redirect intent.
     */
    fun post(oAuthAction: OAuthAction)

    /**
     * Observes the published actions; `null` is emitted when there is nothing pending.
     *
     * @param collector the collector to feed.
     */
    suspend fun collect(collector: FlowCollector<OAuthAction?>)

    /** Clears the pending action, so it is not handled twice. */
    fun reset()
}
