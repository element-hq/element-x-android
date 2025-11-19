/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.external

/***
 * Represents a session data of a session created by another client.
 */
data class ExternalSession(
    val userId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String?,
    val homeserverUrl: String,
)
