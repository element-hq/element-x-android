/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.api

import java.util.Date

/**
 * Data class representing the session data to store locally.
 */
data class SessionData(
    /** The user ID of the logged in user. */
    val userId: String,
    /** The device ID of the session. */
    val deviceId: String,
    /** The current access token of the session. */
    val accessToken: String,
    /** The optional current refresh token of the session. */
    val refreshToken: String?,
    /** The homeserver URL of the session. */
    val homeserverUrl: String,
    /** The Open ID Connect info for this session, if any. */
    val oidcData: String?,
    /** The timestamp of the last login. May be `null` in very old sessions. */
    val loginTimestamp: Date?,
    /** Whether the [accessToken] is valid or not. */
    val isTokenValid: Boolean,
    /** The login type used to authenticate the session. */
    val loginType: LoginType,
    /** The optional passphrase used to encrypt data in the SDK local store. */
    val passphrase: String?,
    /** The paths to the session data stored in the filesystem. */
    val sessionPath: String,
    /** The path to the cache data stored for the session in the filesystem. */
    val cachePath: String,
    /** The position, to be able to order account. */
    val position: Long,
    /** The index of the last date of session usage. */
    val lastUsageIndex: Long,
    /** The optional display name of the user. */
    val userDisplayName: String?,
    /** The optional avatar URL of the user. */
    val userAvatarUrl: String?,
)
