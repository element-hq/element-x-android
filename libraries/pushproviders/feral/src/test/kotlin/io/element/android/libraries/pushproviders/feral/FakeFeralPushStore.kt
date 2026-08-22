/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFeralPushStore(
    initial: List<FeralPushRegistration> = emptyList(),
) : FeralPushStore {
    val state = MutableStateFlow(initial.associateBy { it.session })

    override val registrations: Flow<List<FeralPushRegistration>> = state.map { it.values.toList() }

    override suspend fun get(sessionId: SessionId): FeralPushRegistration? = state.value[sessionId]

    override suspend fun set(registration: FeralPushRegistration) {
        state.update { it + (registration.session to registration) }
    }

    override suspend fun remove(sessionId: SessionId) {
        state.update { it - sessionId }
    }

    override suspend fun setLastMessageId(sessionId: SessionId, messageId: String) {
        state.update { map ->
            val current = map[sessionId] ?: return@update map
            map + (sessionId to current.copy(lastMessageId = messageId))
        }
    }
}

fun aFeralPushRegistration(
    sessionId: SessionId,
    topic: String = "up0123456789abcdef0123456789abcdef",
    clientSecret: String = "aClientSecret",
    lastMessageId: String? = null,
) = FeralPushRegistration(
    sessionId = sessionId.value,
    topic = topic,
    endpoint = FeralPushProvider.endpointFor(topic),
    clientSecret = clientSecret,
    lastMessageId = lastMessageId,
)
