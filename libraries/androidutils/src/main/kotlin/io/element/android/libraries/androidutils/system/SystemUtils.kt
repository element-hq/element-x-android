/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.annotation.RequiresApi
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import io.element.android.libraries.androidutils.R
import io.element.android.libraries.androidutils.compat.getApplicationInfoCompat
import io.element.android.libraries.core.mimetype.MimeTypes

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
 * Retrieve the versionCode from the Manifest.
 * The value is more accurate than BuildConfig.VERSION_CODE, as it is correct according to the
 * computation in the `androidComponents` block of the app build.gradle.kts file.
 * In other words, the last digit (for the architecture) will be set, whereas BuildConfig.VERSION_CODE
 * last digit will always be 0.
 */
fun Context.getVersionCodeFromManifest(): Long {
    return PackageInfoCompat.getLongVersionCode(
        packageManager.getPackageInfo(packageName, 0)
    )
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
fun Context.startNotificationSettingsIntent(
    activityResultLauncher: ActivityResultLauncher<Intent>? = null,
    noActivityFoundMessage: String = getString(R.string.error_no_compatible_app_found),
) {
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
        .setData("package:$packageName".toUri())
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
) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    if (this !is Activity) {
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
