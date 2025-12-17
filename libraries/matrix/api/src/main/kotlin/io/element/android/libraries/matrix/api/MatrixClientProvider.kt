/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

import io.element.android.libraries.matrix.api.core.SessionId

interface MatrixClientProvider {
    /**
     * Can be used to get or restore a MatrixClient with the given [SessionId].
     * If a [MatrixClient] is already in memory, it'll return it. Otherwise it'll try to restore one.
     * Most of the time you want to use injected constructor instead of retrieving a MatrixClient with this provider.
     */
    suspend fun getOrRestore(sessionId: SessionId): Result<MatrixClient>

    /**
     * Can be used to retrieve an existing [MatrixClient] with the given [SessionId].
     * @param sessionId the [SessionId] of the [MatrixClient] to retrieve.
     * @return the [MatrixClient] if it exists.
     */
    fun getOrNull(sessionId: SessionId): MatrixClient?
}
