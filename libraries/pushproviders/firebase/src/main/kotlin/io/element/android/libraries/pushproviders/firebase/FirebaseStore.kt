/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * This class store the Firebase token in SharedPrefs.
 */
interface FirebaseStore {
    fun getFcmToken(): String?
    fun fcmTokenFlow(): Flow<String?>
    fun storeFcmToken(token: String?)
}

@ContributesBinding(AppScope::class)
@Inject class SharedPreferencesFirebaseStore(
    private val sharedPreferences: SharedPreferences,
) : FirebaseStore {
    override fun getFcmToken(): String? {
        return sharedPreferences.getString(PREFS_KEY_FCM_TOKEN, null)
    }

    override fun fcmTokenFlow(): Flow<String?> {
        val flow = MutableStateFlow(getFcmToken())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (k == PREFS_KEY_FCM_TOKEN) {
                try {
                    flow.value = getFcmToken()
                } catch (e: Exception) {
                    flow.value = null
                }
            }
        }
        return flow
            .onStart { sharedPreferences.registerOnSharedPreferenceChangeListener(listener) }
            .onCompletion { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun storeFcmToken(token: String?) {
        sharedPreferences.edit {
            putString(PREFS_KEY_FCM_TOKEN, token)
        }
    }

    companion object {
        private const val PREFS_KEY_FCM_TOKEN = "FCM_TOKEN"
    }
}
