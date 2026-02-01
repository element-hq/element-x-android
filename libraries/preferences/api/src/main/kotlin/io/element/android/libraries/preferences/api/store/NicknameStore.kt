/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.flow.Flow

interface NicknameStore {
    suspend fun setNickname(userId: UserId, nickname: String?)
    fun getNickname(userId: UserId): Flow<String?>
    fun getAllNicknames(): Flow<Map<String, String>>
    suspend fun clear()
}
