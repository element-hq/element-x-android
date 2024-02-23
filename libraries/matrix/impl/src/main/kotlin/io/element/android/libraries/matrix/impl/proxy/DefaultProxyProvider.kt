/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.proxy

import android.content.Context
import android.provider.Settings
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

/**
 * Provides the proxy settings from the system.
 * Note that you can configure the global proxy using adb like this:
 * ```
 * adb shell settings put global http_proxy https://proxy.example.com:8080
 * ```
 * and to remove it:
 * ```
 * adb shell settings delete global http_proxy
 * ```
 */
@ContributesBinding(AppScope::class)
class DefaultProxyProvider @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ProxyProvider {
    override fun provides(): String? {
        return Settings.Global.getString(context.contentResolver, Settings.Global.HTTP_PROXY)
    }
}
