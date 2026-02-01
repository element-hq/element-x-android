/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.preferences.api.store.NicknameStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNicknameStore : NicknameStore {
    private val nicknames = MutableStateFlow<Map<UserId, String>>(emptyMap())

    override suspend fun setNickname(userId: UserId, nickname: String?) {
        val current = nicknames.value.toMutableMap()
        if (nickname == null) {
            current.remove(userId)
        } else {
            current[userId] = nickname
        }
        nicknames.value = current
    }

    override fun getNickname(userId: UserId): Flow<String?> {
        return nicknames.map { it[userId] }
    }

    override fun getAllNicknames(): Flow<Map<String, String>> {
        return nicknames.map { map ->
            map.mapKeys { it.key.value }
        }
    }

    override suspend fun clear() {
        nicknames.value = emptyMap()
    }
}
