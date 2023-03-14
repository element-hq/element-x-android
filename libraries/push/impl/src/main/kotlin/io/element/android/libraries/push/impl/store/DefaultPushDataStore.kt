package io.element.android.libraries.push.impl.store

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.api.model.BackgroundSyncMode
import io.element.android.libraries.push.api.store.PushDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "push_store")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPushDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultPreferences private val defaultPrefs: SharedPreferences,
) : PushDataStore {
    private val pushCounter = intPreferencesKey("push_counter")

    override val pushCounterFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[pushCounter] ?: 0
    }

    suspend fun incrementPushCounter() {
        context.dataStore.edit { settings ->
            val currentCounterValue = settings[pushCounter] ?: 0
            settings[pushCounter] = currentCounterValue + 1
        }
    }

    override fun areNotificationEnabledForDevice(): Boolean {
        return defaultPrefs.getBoolean(SETTINGS_ENABLE_THIS_DEVICE_PREFERENCE_KEY, true)
    }

    override fun setNotificationEnabledForDevice(enabled: Boolean) {
        defaultPrefs.edit {
            putBoolean(SETTINGS_ENABLE_THIS_DEVICE_PREFERENCE_KEY, enabled)
        }
    }

    override fun backgroundSyncTimeOut(): Int {
        return tryOrNull {
            // The xml pref is saved as a string so use getString and parse
            defaultPrefs.getString(SETTINGS_SET_SYNC_TIMEOUT_PREFERENCE_KEY, null)?.toInt()
        } ?: BackgroundSyncMode.DEFAULT_SYNC_TIMEOUT_SECONDS
    }

    override fun setBackgroundSyncTimeout(timeInSecond: Int) {
        defaultPrefs
            .edit()
            .putString(SETTINGS_SET_SYNC_TIMEOUT_PREFERENCE_KEY, timeInSecond.toString())
            .apply()
    }

    override fun backgroundSyncDelay(): Int {
        return tryOrNull {
            // The xml pref is saved as a string so use getString and parse
            defaultPrefs.getString(SETTINGS_SET_SYNC_DELAY_PREFERENCE_KEY, null)?.toInt()
        } ?: BackgroundSyncMode.DEFAULT_SYNC_DELAY_SECONDS
    }

    override fun setBackgroundSyncDelay(timeInSecond: Int) {
        defaultPrefs
            .edit()
            .putString(SETTINGS_SET_SYNC_DELAY_PREFERENCE_KEY, timeInSecond.toString())
            .apply()
    }

    override fun isBackgroundSyncEnabled(): Boolean {
        return getFdroidSyncBackgroundMode() != BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_DISABLED
    }

    override fun setFdroidSyncBackgroundMode(mode: BackgroundSyncMode) {
        defaultPrefs
            .edit()
            .putString(SETTINGS_FDROID_BACKGROUND_SYNC_MODE, mode.name)
            .apply()
    }

    override fun getFdroidSyncBackgroundMode(): BackgroundSyncMode {
        return try {
            val strPref = defaultPrefs
                .getString(SETTINGS_FDROID_BACKGROUND_SYNC_MODE, BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_BATTERY.name)
            BackgroundSyncMode.values().firstOrNull { it.name == strPref } ?: BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_BATTERY
        } catch (e: Throwable) {
            BackgroundSyncMode.FDROID_BACKGROUND_SYNC_MODE_FOR_BATTERY
        }
    }

    /**
     * Return true if Pin code is disabled, or if user set the settings to see full notification content.
     */
    override fun useCompleteNotificationFormat(): Boolean {
        return true
        /*
    return !useFlagPinCode() ||
        defaultPrefs.getBoolean(SETTINGS_SECURITY_USE_COMPLETE_NOTIFICATIONS_FLAG, true)
         */
    }

    companion object {
        // notifications
        const val SETTINGS_ENABLE_ALL_NOTIF_PREFERENCE_KEY = "SETTINGS_ENABLE_ALL_NOTIF_PREFERENCE_KEY"
        const val SETTINGS_ENABLE_THIS_DEVICE_PREFERENCE_KEY = "SETTINGS_ENABLE_THIS_DEVICE_PREFERENCE_KEY"

        // background sync
        const val SETTINGS_START_ON_BOOT_PREFERENCE_KEY = "SETTINGS_START_ON_BOOT_PREFERENCE_KEY"
        const val SETTINGS_ENABLE_BACKGROUND_SYNC_PREFERENCE_KEY = "SETTINGS_ENABLE_BACKGROUND_SYNC_PREFERENCE_KEY"
        const val SETTINGS_SET_SYNC_TIMEOUT_PREFERENCE_KEY = "SETTINGS_SET_SYNC_TIMEOUT_PREFERENCE_KEY"
        const val SETTINGS_SET_SYNC_DELAY_PREFERENCE_KEY = "SETTINGS_SET_SYNC_DELAY_PREFERENCE_KEY"

        const val SETTINGS_FDROID_BACKGROUND_SYNC_MODE = "SETTINGS_FDROID_BACKGROUND_SYNC_MODE"
        const val SETTINGS_BACKGROUND_SYNC_PREFERENCE_KEY = "SETTINGS_BACKGROUND_SYNC_PREFERENCE_KEY"

        const val SETTINGS_SECURITY_USE_COMPLETE_NOTIFICATIONS_FLAG = "SETTINGS_SECURITY_USE_COMPLETE_NOTIFICATIONS_FLAG"

        // notification method
        const val SETTINGS_NOTIFICATION_METHOD_KEY = "SETTINGS_NOTIFICATION_METHOD_KEY"
    }
}
