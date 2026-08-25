/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.api.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Manages the per-session client secret sent along with a pusher registration.
 *
 * Push gateways deliver notifications without saying which account they belong to, so the secret is what lets an incoming push be attributed to a session.
 */
interface PushClientSecret {
    /**
     * To call when registering a pusher. It will return the existing secret or create a new one.
     *
     * @param userId the user the pusher is being registered for.
     */
    suspend fun getSecretForUser(userId: SessionId): String

    /**
     * To call when receiving a push containing a client secret.
     * Return null if not found.
     *
     * @param clientSecret the secret carried by the incoming push.
     */
    suspend fun getUserIdFromSecret(clientSecret: String): SessionId?
}
