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

package io.element.android.services.toolbox.impl.appname

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.getApplicationLabel
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.toolbox.api.appname.AppNameProvider
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultAppNameProvider @Inject constructor(@ApplicationContext private val context: Context) :
    AppNameProvider {

    override fun getAppName(): String {
        return try {
            val appPackageName = context.packageName
            var appName = context.getApplicationLabel(appPackageName)

            // Use appPackageName instead of appName if appName contains any non-ASCII character
            if (!appName.matches("\\A\\p{ASCII}*\\z".toRegex())) {
                appName = appPackageName
            }
            appName
        } catch (e: Exception) {
            Timber.e(e, "## AppNameProvider() : failed")
            "ElementAndroid"
        }
    }
}
