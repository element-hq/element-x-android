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
