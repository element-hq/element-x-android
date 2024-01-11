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

package io.element.android.features.lockscreen.impl.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.LockScreenConfig
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pin_code_store")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PreferencesLockScreenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockScreenConfig: LockScreenConfig,
) : LockScreenStore {
    private val pinCodeKey = stringPreferencesKey("encoded_pin_code")
    private val remainingAttemptsKey = intPreferencesKey("remaining_pin_code_attempts")
    private val biometricUnlockKey = booleanPreferencesKey("biometric_unlock_enabled")

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return context.dataStore.data.map { preferences ->
            preferences.getRemainingPinCodeAttemptsNumber()
        }.first()
    }

    override suspend fun onWrongPin() {
        context.dataStore.edit { preferences ->
            val current = preferences.getRemainingPinCodeAttemptsNumber()
            val remaining = (current - 1).coerceAtLeast(0)
            preferences[remainingAttemptsKey] = remaining
        }
    }

    override suspend fun resetCounter() {
        context.dataStore.edit { preferences ->
            preferences[remainingAttemptsKey] = lockScreenConfig.maxPinCodeAttemptsBeforeLogout
        }
    }

    override suspend fun getEncryptedCode(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[pinCodeKey]
        }.first()
    }

    override suspend fun saveEncryptedPinCode(pinCode: String) {
        context.dataStore.edit { preferences ->
            preferences[pinCodeKey] = pinCode
        }
    }

    override suspend fun deleteEncryptedPinCode() {
        context.dataStore.edit { preferences ->
            preferences.remove(pinCodeKey)
        }
    }

    override fun hasPinCode(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[pinCodeKey] != null
        }
    }

    override fun isBiometricUnlockAllowed(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[biometricUnlockKey] ?: false
        }
    }

    override suspend fun setIsBiometricUnlockAllowed(isAllowed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[biometricUnlockKey] = isAllowed
        }
    }

    private fun Preferences.getRemainingPinCodeAttemptsNumber() = this[remainingAttemptsKey] ?: lockScreenConfig.maxPinCodeAttemptsBeforeLogout
}
