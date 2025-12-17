/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.storage

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class PreferencesLockScreenStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
    private val lockScreenConfig: LockScreenConfig,
) : LockScreenStore {
    private val dataStore = preferenceDataStoreFactory.create("pin_code_store")

    private val pinCodeKey = stringPreferencesKey("encoded_pin_code")
    private val remainingAttemptsKey = intPreferencesKey("remaining_pin_code_attempts")
    private val biometricUnlockKey = booleanPreferencesKey("biometric_unlock_enabled")

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return dataStore.data.map { preferences ->
            preferences.getRemainingPinCodeAttemptsNumber()
        }.first()
    }

    override suspend fun onWrongPin() {
        dataStore.edit { preferences ->
            val current = preferences.getRemainingPinCodeAttemptsNumber()
            val remaining = (current - 1).coerceAtLeast(0)
            preferences[remainingAttemptsKey] = remaining
        }
    }

    override suspend fun resetCounter() {
        dataStore.edit { preferences ->
            preferences[remainingAttemptsKey] = lockScreenConfig.maxPinCodeAttemptsBeforeLogout
        }
    }

    override suspend fun getEncryptedCode(): String? {
        return dataStore.data.map { preferences ->
            preferences[pinCodeKey]
        }.first()
    }

    override suspend fun saveEncryptedPinCode(pinCode: String) {
        dataStore.edit { preferences ->
            preferences[pinCodeKey] = pinCode
        }
    }

    override suspend fun deleteEncryptedPinCode() {
        dataStore.edit { preferences ->
            preferences.remove(pinCodeKey)
        }
    }

    override fun hasPinCode(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[pinCodeKey] != null
        }
    }

    override fun isBiometricUnlockAllowed(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[biometricUnlockKey] ?: false
        }
    }

    override suspend fun setIsBiometricUnlockAllowed(isAllowed: Boolean) {
        dataStore.edit { preferences ->
            preferences[biometricUnlockKey] = isAllowed
        }
    }

    private fun Preferences.getRemainingPinCodeAttemptsNumber() = this[remainingAttemptsKey] ?: lockScreenConfig.maxPinCodeAttemptsBeforeLogout
}
