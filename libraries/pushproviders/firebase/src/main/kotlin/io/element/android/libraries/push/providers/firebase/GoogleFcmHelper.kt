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

package io.element.android.libraries.push.providers.firebase

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DefaultPreferences
import javax.inject.Inject

/**
 * This class store the FCM token in SharedPrefs and ensure this token is retrieved.
 * It has an alter ego in the fdroid variant.
 */
// TODO Rename to store?
class GoogleFcmHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultPreferences private val sharedPrefs: SharedPreferences,
) {
    fun getFcmToken(): String? {
        return sharedPrefs.getString(PREFS_KEY_FCM_TOKEN, null)
    }

    fun storeFcmToken(token: String?) {
        sharedPrefs.edit {
            putString(PREFS_KEY_FCM_TOKEN, token)
        }
    }

    /*
    override fun ensureFcmTokenIsRetrieved(pushersManager: PushersManager, registerPusher: Boolean) {
        // 'app should always check the device for a compatible Google Play services APK before accessing Google Play services features'
        if (checkPlayServices(context)) {
            try {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        storeFcmToken(token)
                        if (registerPusher) {
                            runBlocking {// TODO
                                pushersManager.enqueueRegisterPusherWithFcmKey(token)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "## ensureFcmTokenIsRetrieved() : failed")
                    }
            } catch (e: Throwable) {
                Timber.e(e, "## ensureFcmTokenIsRetrieved() : failed")
            }
        } else {
            Toast.makeText(context, R.string.push_no_valid_google_play_services_apk_android, Toast.LENGTH_SHORT).show()
            Timber.e("No valid Google Play Services found. Cannot use FCM.")
        }
    }
     */

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    /*
    override fun onEnterForeground(activeSessionHolder: ActiveSessionHolder) {
        // No op
    }

    override fun onEnterBackground(activeSessionHolder: ActiveSessionHolder) {
        // No op
    }
     */

    companion object {
        private const val PREFS_KEY_FCM_TOKEN = "FCM_TOKEN"
    }
}
