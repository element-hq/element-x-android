/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

/**
 * Factory to create a temporary [TemporaryMatrixClient] for a given home server URL.
 */
interface TemporaryMatrixClientFactory {
    /**
     * Builds an unauthenticated client pointing at [homeServerUrl], backed by a fresh set of temporary session directories.
     * The caller owns the returned client and must close it to delete those directories.
     *
     * @param homeServerUrl the URL of the homeserver the client will talk to.
     */
    suspend fun create(homeServerUrl: String): Result<TemporaryMatrixClient>
}
