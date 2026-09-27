/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api.localnetwork

interface LocalNetworkPermissionAdvisor {
    /**
     * Returns true when the app should request the ACCESS_LOCAL_NETWORK permission before making
     * network requests to [homeserverUrl].
     */
    suspend fun shouldRequestPermissionFor(homeserverUrl: String): Boolean
}
