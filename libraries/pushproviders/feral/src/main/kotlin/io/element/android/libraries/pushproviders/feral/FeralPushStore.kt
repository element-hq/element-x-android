/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * One registration per session: the ntfy topic the pusher targets, the endpoint used as `pushkey`,
 * the client secret the pusher carries (to map incoming pushes back to the session) and the id of
 * the last ntfy message handled (to replay missed ones with `?since=`).
 */
@Serializable
data class FeralPushRegistration(
    val sessionId: String,
    val topic: String,
    val endpoint: String,
    val clientSecret: String,
    val lastMessageId: String? = null,
) {
    val session: SessionId get() = SessionId(sessionId)
}

interface FeralPushStore {
    /** All registrations, emitted again whenever one is added, removed or updated. */
    val registrations: Flow<List<FeralPushRegistration>>

    suspend fun get(sessionId: SessionId): FeralPushRegistration?

    suspend fun set(registration: FeralPushRegistration)

    suspend fun remove(sessionId: SessionId)

    suspend fun setLastMessageId(sessionId: SessionId, messageId: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFeralPushStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
    private val jsonProvider: JsonProvider,
) : FeralPushStore {
    private val store = preferenceDataStoreFactory.create("feral_push")

    override val registrations: Flow<List<FeralPushRegistration>> = store.data
        .map { preferences -> preferences.registrations() }
        .distinctUntilChanged()

    override suspend fun get(sessionId: SessionId): FeralPushRegistration? {
        return store.data.first().registration(sessionId)
    }

    override suspend fun set(registration: FeralPushRegistration) {
        store.edit { it[key(registration.session)] = jsonProvider().encodeToString(registration) }
    }

    override suspend fun remove(sessionId: SessionId) {
        store.edit { it.remove(key(sessionId)) }
    }

    override suspend fun setLastMessageId(sessionId: SessionId, messageId: String) {
        store.edit { preferences ->
            val current = preferences.registration(sessionId) ?: return@edit
            preferences[key(sessionId)] = jsonProvider().encodeToString(current.copy(lastMessageId = messageId))
        }
    }

    private fun Preferences.registrations(): List<FeralPushRegistration> {
        return asMap().entries
            .filter { it.key.name.startsWith(KEY_PREFIX) }
            .mapNotNull { (_, value) -> (value as? String)?.let(::decode) }
    }

    private fun Preferences.registration(sessionId: SessionId): FeralPushRegistration? {
        return this[key(sessionId)]?.let(::decode)
    }

    private fun decode(value: String): FeralPushRegistration? {
        return tryOrNull { jsonProvider().decodeFromString<FeralPushRegistration>(value) }
    }

    private fun key(sessionId: SessionId) = stringPreferencesKey(KEY_PREFIX + sessionId.value)

    private companion object {
        const val KEY_PREFIX = "registration_"
    }
}
