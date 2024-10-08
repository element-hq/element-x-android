/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.browser

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import io.element.android.libraries.androidutils.system.openUrlInExternalApp

/**
 * Open url in custom tab or, if not available, in the default browser.
 * If several compatible browsers are installed, the user will be proposed to choose one.
 * Ref: https://developer.chrome.com/multidevice/android/customtabs.
 */
fun Activity.openUrlInChromeCustomTab(
    session: CustomTabsSession?,
    darkTheme: Boolean,
    url: String
) {
    try {
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    // TODO .setToolbarColor(ThemeUtils.getColor(context, android.R.attr.colorBackground))
                    // TODO .setNavigationBarColor(ThemeUtils.getColor(context, android.R.attr.colorBackground))
                    .build()
            )
            .setColorScheme(
                when (darkTheme) {
                    false -> CustomTabsIntent.COLOR_SCHEME_LIGHT
                    true -> CustomTabsIntent.COLOR_SCHEME_DARK
                }
            )
            .setShareIdentityEnabled(false)
            // Note: setting close button icon does not work
            // .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_back_24dp))
            // .setStartAnimations(context, R.anim.enter_fade_in, R.anim.exit_fade_out)
            // .setExitAnimations(context, R.anim.enter_fade_in, R.anim.exit_fade_out)
            .apply { session?.let { setSession(it) } }
            .build()
            .apply {
                // Disable download button
                intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_DOWNLOAD_BUTTON", true)
                // Disable bookmark button
                intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_START_BUTTON", true)
            }
            .launchUrl(this, Uri.parse(url))
    } catch (activityNotFoundException: ActivityNotFoundException) {
        openUrlInExternalApp(url)
    }
}
