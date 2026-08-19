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
 * Persists the mapping between sessions and their push client secrets, in both directions.
 */
interface PushClientSecretStore {
    /**
     * Stores the secret of a user, replacing any previous one.
     *
     * @param userId the user the secret belongs to.
     * @param clientSecret the secret to store.
     */
    suspend fun storeSecret(userId: SessionId, clientSecret: String)

    /**
     * Returns the stored secret of a user, or `null` when they have none yet.
     *
     * @param userId the user whose secret is requested.
     */
    suspend fun getSecret(userId: SessionId): String?

    /**
     * Forgets the secret of a user, to be called when their session is removed.
     *
     * @param userId the user whose secret is erased.
     */
    suspend fun resetSecret(userId: SessionId)

    /**
     * Reverse lookup used when a push arrives carrying only a secret.
     *
     * @param clientSecret the secret to resolve.
     * @return the user it belongs to, or `null` when no session matches.
     */
    suspend fun getUserIdFromSecret(clientSecret: String): SessionId?
}
