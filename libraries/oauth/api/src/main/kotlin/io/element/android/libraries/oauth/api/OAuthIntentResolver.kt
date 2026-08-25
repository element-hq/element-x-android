/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.api

import android.content.Intent

/**
 * Recognises the redirect the authentication server sends the user back with once they have signed in or cancelled.
 */
interface OAuthIntentResolver {
    /**
     * @param intent the intent the app was resumed with.
     * @return the action to carry out, or `null` when the intent is not an OAuth redirect.
     */
    fun resolve(intent: Intent): OAuthAction?
}
