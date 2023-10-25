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

package io.element.android.features.lockscreen.impl.pin.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.LockScreenConfig
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pin_code_store")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PreferencesPinCodeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : PinCodeStore {

    private val pinCodeKey = stringPreferencesKey("encoded_pin_code")
    private val remainingAttemptsKey = intPreferencesKey("remaining_pin_code_attempts")

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[remainingAttemptsKey] ?: 0
        }.first()
    }

    override suspend fun onWrongPin() {
        context.dataStore.edit { preferences ->
            val current = preferences[remainingAttemptsKey] ?: 0
            val remaining = (current - 1).coerceAtLeast(0)
            preferences[remainingAttemptsKey] = remaining
        }
    }

    override suspend fun resetCounter() {
        context.dataStore.edit { preferences ->
            preferences[remainingAttemptsKey] = LockScreenConfig.MAX_PIN_CODE_ATTEMPTS_NUMBER_BEFORE_LOGOUT
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

    override suspend fun hasPinCode(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[pinCodeKey] != null
        }.first()
    }
}
