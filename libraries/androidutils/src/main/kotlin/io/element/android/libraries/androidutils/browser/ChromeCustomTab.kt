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

package io.element.android.libraries.androidutils.browser

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession

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
        // TODO context.toast(R.string.error_no_external_application_found)
    }
}
