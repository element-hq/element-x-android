/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * This class stores the Firebase installationId in SharedPrefs.
 */
interface FirebaseStore {
    fun getInstallationId(): String?
    fun fcmInstallationIdFlow(): Flow<String?>
    fun storeInstallationId(installationId: String?)
}

@ContributesBinding(AppScope::class)
class SharedPreferencesFirebaseStore(
    private val sharedPreferences: SharedPreferences,
) : FirebaseStore {
    override fun getInstallationId(): String? {
        return sharedPreferences.getString(PREFS_KEY_FCM_INSTALLATION_ID, null)
    }

    override fun fcmInstallationIdFlow(): Flow<String?> {
        val flow = MutableStateFlow(getInstallationId())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (k == PREFS_KEY_FCM_INSTALLATION_ID) {
                try {
                    flow.value = getInstallationId()
                } catch (_: Exception) {
                    flow.value = null
                }
            }
        }
        return flow
            .onStart { sharedPreferences.registerOnSharedPreferenceChangeListener(listener) }
            .onCompletion { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun storeInstallationId(installationId: String?) {
        sharedPreferences.edit {
            putString(PREFS_KEY_FCM_INSTALLATION_ID, installationId)
        }
    }

    companion object {
        private const val PREFS_KEY_FCM_INSTALLATION_ID = "FCM_TOKEN"
    }
}
