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

package io.element.android.libraries.androidutils.system

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import io.element.android.libraries.androidutils.R
import io.element.android.libraries.androidutils.compat.getApplicationInfoCompat
import io.element.android.libraries.core.mimetype.MimeTypes

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

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

// ==============================================================================================================
// Clipboard helper
// ==============================================================================================================

/**
 * Copy a text to the clipboard, and display a Toast when done.
 *
 * @receiver the context
 * @param text the text to copy
 * @param toastMessage content of the toast message as a String resource. Null for no toast
 */
fun Context.copyToClipboard(
    text: CharSequence,
    toastMessage: String? = null
) {
    CopyToClipboardUseCase(this).execute(text)
    toastMessage?.let { toast(it) }
}

/**
 * Shows notification settings for the current app.
 * In android O will directly opens the notification settings, in lower version it will show the App settings
 */
fun Context.startNotificationSettingsIntent(activityResultLauncher: ActivityResultLauncher<Intent>? = null) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        if (this !is Activity && activityResultLauncher == null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.fromParts("package", packageName, null)
    }

    if (activityResultLauncher != null) {
        activityResultLauncher.launch(intent)
    } else {
        startActivity(intent)
    }
}

fun Context.openAppSettingsPage(
    noActivityFoundMessage: String = getString(R.string.error_no_compatible_app_found),
) {
    try {
        startActivity(
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", packageName, null)
            }
        )
    } catch (activityNotFoundException: ActivityNotFoundException) {
        toast(noActivityFoundMessage)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.startInstallFromSourceIntent(
    activityResultLauncher: ActivityResultLauncher<Intent>,
    noActivityFoundMessage: String = getString(R.string.error_no_compatible_app_found),
) {
    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        .setData(Uri.parse("package:$packageName"))
    try {
        activityResultLauncher.launch(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        toast(noActivityFoundMessage)
    }
}

fun Context.startSharePlainTextIntent(
    activityResultLauncher: ActivityResultLauncher<Intent>?,
    chooserTitle: String?,
    text: String,
    subject: String? = null,
    extraTitle: String? = null,
    noActivityFoundMessage: String = getString(R.string.error_no_compatible_app_found),
) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = MimeTypes.PlainText
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
            startActivity(intent)
        }
    } catch (activityNotFoundException: ActivityNotFoundException) {
        toast(noActivityFoundMessage)
    }
}

@Suppress("SwallowedException")
fun Context.openUrlInExternalApp(
    url: String,
    errorMessage: String = getString(R.string.error_no_compatible_app_found),
    inNewTask: Boolean = false,
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (inNewTask) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        toast(errorMessage)
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
