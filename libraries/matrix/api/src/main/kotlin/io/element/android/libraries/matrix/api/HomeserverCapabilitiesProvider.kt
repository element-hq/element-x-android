/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

/**
 * Provides information about the capabilities of the homeserver.
 *
 * The getters read a snapshot held by the SDK rather than querying the server on each call, so [refresh] must be used to pick up server-side changes.
 *
 * Spec: https://spec.matrix.org/latest/client-server-api/#capabilities-negotiation
 */
interface HomeserverCapabilitiesProvider {
    /**
     * Manually refresh the capabilities of the homeserver performing a network request.
     */
    suspend fun refresh(): Result<Unit>

    /**
     * Indicates whether the homeserver allows the user to change their display name.
     */
    suspend fun canChangeDisplayName(): Result<Boolean>

    /**
     * Indicates whether the homeserver allows the user to change their avatar URL.
     */
    suspend fun canChangeAvatarUrl(): Result<Boolean>
}
