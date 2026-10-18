/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api.localnetwork

/**
 * Decides whether the local network permission is needed to reach a homeserver, which is the case for a server on the user's own network.
 */
interface LocalNetworkPermissionAdvisor {
    /**
     * Returns true when the app should request the ACCESS_LOCAL_NETWORK permission before making
     * network requests to [homeserverUrl].
     *
     * @param homeserverUrl the server the app is about to contact.
     */
    suspend fun shouldRequestPermissionFor(homeserverUrl: String): Boolean
}
