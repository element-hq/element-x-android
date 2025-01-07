/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.api.clientsecret

import io.element.android.libraries.matrix.api.core.SessionId

interface PushClientSecretStore {
    suspend fun storeSecret(userId: SessionId, clientSecret: String)
    suspend fun getSecret(userId: SessionId): String?
    suspend fun resetSecret(userId: SessionId)
    suspend fun getUserIdFromSecret(clientSecret: String): SessionId?
}
