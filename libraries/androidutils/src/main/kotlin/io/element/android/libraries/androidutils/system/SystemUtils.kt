/*
 * Copyright 2018 New Vector Ltd
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

package io.element.android.libraries.androidutils.system

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import io.element.android.libraries.androidutils.compat.getApplicationInfoCompat

/**
 * Tells if the application ignores battery optimizations.
 *
 * Ignoring them allows the app to run in background to make background sync with the homeserver.
 * This user option appears on Android M but Android O enforces its usage and kills apps not
 * authorised by the user to run in background.
 *
 * @return true if battery optimisations are ignored
 */
fun Context.isIgnoringBatteryOptimizations(): Boolean {
    // no issue before Android M, battery optimisations did not exist
    return getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) == true
}

fun Context.isAirplaneModeOn(): Boolean {
    return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
}

fun Context.isAnimationEnabled(): Boolean {
    return Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun supportNotificationChannels() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

/**
 * Return the application label of the provided package. If not found, the package is returned.
 */
fun Context.getApplicationLabel(packageName: String): String {
    return try {
        val ai = packageManager.getApplicationInfoCompat(packageName, 0)
        packageManager.getApplicationLabel(ai).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}

/**
 * display the system dialog for granting this permission. If previously granted, the
 * system will not show it (so you should call this method).
 *
 * Note: If the user finally does not grant the permission, PushManager.isBackgroundSyncAllowed()
 * will return false and the notification privacy will fallback to "LOW_DETAIL".
 */
fun requestDisablingBatteryOptimization(activity: Activity, activityResultLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent()
    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    intent.data = Uri.parse("package:" + activity.packageName)
    activityResultLauncher.launch(intent)
}

// ==============================================================================================================
// Clipboard helper
// ==============================================================================================================

/**
 * Copy a text to the clipboard, and display a Toast when done.
 *
 * @param context the context
 * @param text the text to copy
 * @param toastMessage content of the toast message as a String resource. Null for no toast
 */
fun copyToClipboard(
    context: Context,
    text: CharSequence,
    toastMessage: String? = null
) {
    CopyToClipboardUseCase(context).execute(text)
    toastMessage?.let { context.toast(it) }
}

/**
 * Shows notification settings for the current app.
 * In android O will directly opens the notification settings, in lower version it will show the App settings
 */
fun startNotificationSettingsIntent(context: Context, activityResultLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra("app_package", context.packageName)
        intent.putExtra("app_uid", context.applicationInfo?.uid)
    }
    activityResultLauncher.launch(intent)
}

/**
 * Shows notification system settings for the given channel id.
 */
@TargetApi(Build.VERSION_CODES.O)
fun startNotificationChannelSettingsIntent(activity: Activity, channelID: String) {
    if (!supportNotificationChannels()) return
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, channelID)
    }
    activity.startActivity(intent)
}

fun startAddGoogleAccountIntent(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    noActivityFoundMessage: String,
) {
    val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
    intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    try {
        activityResultLauncher.launch(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        context.toast(noActivityFoundMessage)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun startInstallFromSourceIntent(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    noActivityFoundMessage: String,
) {
    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        .setData(Uri.parse(String.format("package:%s", context.packageName)))
    try {
        activityResultLauncher.launch(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        context.toast(noActivityFoundMessage)
    }
}

fun startSharePlainTextIntent(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>?,
    chooserTitle: String?,
    text: String,
    subject: String? = null,
    extraTitle: String? = null,
    noActivityFoundMessage: String,
) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "text/plain"
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    // Add data to the intent, the receiving app will decide what to do with it.
    share.putExtra(Intent.EXTRA_SUBJECT, subject)
    share.putExtra(Intent.EXTRA_TEXT, text)

    extraTitle?.let {
        share.putExtra(Intent.EXTRA_TITLE, it)
    }

    val intent = Intent.createChooser(share, chooserTitle)
    try {
        if (activityResultLauncher != null) {
            activityResultLauncher.launch(intent)
        } else {
            context.startActivity(intent)
        }
    } catch (activityNotFoundException: ActivityNotFoundException) {
        context.toast(noActivityFoundMessage)
    }
}

fun startImportTextFromFileIntent(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    noActivityFoundMessage: String,
) {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "text/plain"
    }
    try {
        activityResultLauncher.launch(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        context.toast(noActivityFoundMessage)
    }
}

// Not in KTX anymore
fun Context.toast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

// Not in KTX anymore
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
