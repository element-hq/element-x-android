/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import io.element.android.libraries.matrix.api.MatrixClient

/**
 * Resolves how aggressively media should be compressed, combining the user's own preferences with the app defaults.
 */
fun interface MediaOptimizationConfigProvider {
    /** Returns the configuration to pass to the pre-processing and sending calls. */
    suspend fun get(): MediaOptimizationConfig
}

/**
 * Creates a [MediaOptimizationConfigProvider] for a given session, used outside the session scope, for instance when sharing to another account.
 */
fun interface MediaOptimizationConfigProviderFactory {
    /**
     * Create a [MediaOptimizationConfigProvider] using the preferences of the session of [client].
     *
     * @param client the client of the session whose preferences should be used.
     */
    fun create(client: MatrixClient): MediaOptimizationConfigProvider
}
