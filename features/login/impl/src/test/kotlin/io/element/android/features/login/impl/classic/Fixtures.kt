/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.classic

import android.graphics.Bitmap
import io.element.android.libraries.matrix.api.auth.ElementClassicSession
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_USER_ID

internal const val ROOM_KEYS_VERSION = "roomKeysVersion as Json data"

fun anElementClassicReady(
    elementClassicSession: ElementClassicSession = anElementClassicSession(),
    displayName: String? = null,
    avatar: Bitmap? = null,
) = ElementClassicConnectionState.ElementClassicReady(
    elementClassicSession = elementClassicSession,
    displayName = displayName,
    avatar = avatar,
)

fun anElementClassicSession(
    userId: UserId = A_USER_ID,
    homeserverUrl: String? = null,
    secrets: String? = null,
    roomKeysVersion: String? = null,
    doesContainBackupKey: Boolean = false,
) = ElementClassicSession(
    userId = userId,
    homeserverUrl = homeserverUrl,
    secrets = secrets,
    roomKeysVersion = roomKeysVersion,
    doesContainBackupKey = doesContainBackupKey,
)
