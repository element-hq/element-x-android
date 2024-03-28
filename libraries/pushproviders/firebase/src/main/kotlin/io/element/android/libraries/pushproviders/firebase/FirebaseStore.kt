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

package io.element.android.libraries.pushproviders.firebase

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.DefaultPreferences
import javax.inject.Inject

/**
 * This class store the Firebase token in SharedPrefs.
 */
interface FirebaseStore {
    fun getFcmToken(): String?
    fun storeFcmToken(token: String?)
}

@ContributesBinding(AppScope::class)
class DefaultFirebaseStore @Inject constructor(
    @DefaultPreferences private val sharedPrefs: SharedPreferences,
) : FirebaseStore {
    override fun getFcmToken(): String? {
        return sharedPrefs.getString(PREFS_KEY_FCM_TOKEN, null)
    }

    override fun storeFcmToken(token: String?) {
        sharedPrefs.edit {
            putString(PREFS_KEY_FCM_TOKEN, token)
        }
    }

    companion object {
        private const val PREFS_KEY_FCM_TOKEN = "FCM_TOKEN"
    }
}
