/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

/**
 * Provides the redirect URL registered for OAuth login, which the authentication server sends the user back to once they have signed in.
 */
interface OAuthRedirectUrlProvider {
    /** Returns the redirect URL, built from the app's custom URL scheme so that the system hands the callback back to the app. */
    fun provide(): String
}
