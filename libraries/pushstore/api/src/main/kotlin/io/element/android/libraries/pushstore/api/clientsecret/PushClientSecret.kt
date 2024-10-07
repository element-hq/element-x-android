/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushstore.api.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId

interface PushClientSecret {
    /**
     * To call when registering a pusher. It will return the existing secret or create a new one.
     */
    suspend fun getSecretForUser(userId: SessionId): String

    /**
     * To call when receiving a push containing a client secret.
     * Return null if not found.
     */
    suspend fun getUserIdFromSecret(clientSecret: String): SessionId?
}
