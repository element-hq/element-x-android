/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    /** The Sliding Sync Proxy URL for this session, if any. */
    val slidingSyncProxy: String?,
    /** The timestamp of the last login. May be `null` in very old sessions. */
    val loginTimestamp: Date?,
    /** Whether the [accessToken] is valid or not. */
    val isTokenValid: Boolean,
    /** The login type used to authenticate the session. */
    val loginType: LoginType,
    /** The optional passphrase used to encrypt data in the SDK local store. */
    val passphrase: String?,
)
