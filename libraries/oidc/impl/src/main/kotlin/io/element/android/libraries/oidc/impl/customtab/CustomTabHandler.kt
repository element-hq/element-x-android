/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.customtab

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

class CustomTabHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var customTabsSession: CustomTabsSession? = null
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsServiceConnection: CustomTabsServiceConnection? = null

    fun prepareCustomTab(url: String) {
        val packageName = CustomTabsClient.getPackageName(context, null)

        // packageName can be null if there are 0 or several CustomTabs compatible browsers installed on the device
        if (packageName != null) {
            customTabsServiceConnection = object : CustomTabsServiceConnection() {
                override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                    customTabsClient = client.apply { warmup(0L) }
                    prefetchUrl(url)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
                .also {
                    CustomTabsClient.bindCustomTabsService(
                        context,
                        // Despite the API, packageName cannot be null
                        packageName,
                        it
                    )
                }
        }
    }

    private fun prefetchUrl(url: String) {
        if (customTabsSession == null) {
            customTabsSession = customTabsClient?.newSession(null)
        }

        customTabsSession?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun disposeCustomTab() {
        customTabsServiceConnection?.let { context.unbindService(it) }
        customTabsServiceConnection = null
    }

    fun open(activity: Activity, darkTheme: Boolean, url: String) {
        activity.openUrlInChromeCustomTab(customTabsSession, darkTheme, url)
    }
}
