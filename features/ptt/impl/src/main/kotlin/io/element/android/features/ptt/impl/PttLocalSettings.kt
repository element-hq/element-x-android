/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device preferences plus interim, per-account Mumble connection settings. Passwords stay in memory. */
@SingleIn(AppScope::class)
@Inject
class PttLocalSettings(@ApplicationContext context: Context) {
    private val preferences = preferences(context)
    private val passwords = mutableMapOf<SessionId, String>()
    private val _overlayEnabled = MutableStateFlow(preferences.getBoolean("overlay", true))
    val overlayEnabled = _overlayEnabled.asStateFlow()
    private val _lockScreenEnabled = MutableStateFlow(preferences.getBoolean("lock_screen", true))
    val lockScreenEnabled = _lockScreenEnabled.asStateFlow()
    val covert get() = preferences.getBoolean("covert", false)

    fun setOverlayEnabled(enabled: Boolean) {
        preferences.edit { putBoolean("overlay", enabled) }
        _overlayEnabled.value = enabled
    }

    fun setLockScreenEnabled(enabled: Boolean) {
        preferences.edit { putBoolean("lock_screen", enabled) }
        _lockScreenEnabled.value = enabled
    }
    fun setCovert(enabled: Boolean) = preferences.edit { putBoolean("covert", enabled) }

    fun server(sessionId: SessionId): PttServerSettings {
        val prefix = sessionId.value
        return PttServerSettings(
            host = preferences.getString("$prefix.host", MURMUR_HOST).orEmpty(),
            port = preferences.getString("$prefix.port", "64738").orEmpty(),
            username = preferences.getString("$prefix.username", "").orEmpty(),
            password = passwords[sessionId].orEmpty(),
        )
    }

    fun saveServer(sessionId: SessionId, server: PttServerSettings) {
        require(server.isValid)
        val prefix = sessionId.value
        preferences.edit {
            putString("$prefix.host", server.host.trim())
            putString("$prefix.port", server.port.trim())
            putString("$prefix.username", server.username.trim())
        }
        passwords[sessionId] = server.password
    }

    companion object {
        fun preferences(context: Context) = context.getSharedPreferences("ptt_settings", Context.MODE_PRIVATE)
    }
}

// Deliberately not a data class: its generated toString would expose the password.
class PttServerSettings(val host: String = "", val port: String = "64738", val username: String = "", val password: String = "") {
    override fun toString(): String = "PttServerSettings(redacted)"

    val isValid: Boolean get() = host.trim().let { it.isNotEmpty() && it.none(Char::isWhitespace) && '/' !in it } &&
        port.trim().toIntOrNull() in 1..65_535
}
