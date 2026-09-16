/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.api

/**
 * Extracts the login parameters from a link, so that a deployment can hand out a URL that pre-fills the sign-in screen.
 */
interface LoginIntentResolver {
    /**
     * @param uriString the link the app was opened with.
     * @return the account provider and login hint to pre-fill, or `null` when the link is not a login link.
     */
    fun parse(uriString: String): LoginParams?
}
