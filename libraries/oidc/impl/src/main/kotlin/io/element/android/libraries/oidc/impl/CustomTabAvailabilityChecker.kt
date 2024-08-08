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

package io.element.android.libraries.oidc.impl

import android.content.Context
import androidx.browser.customtabs.CustomTabsClient
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

class CustomTabAvailabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Return true if the device supports Custom tab, i.e. there is an third party app with
     * CustomTab support (ex: Chrome, Firefox, etc.).
     */
    fun supportCustomTab(): Boolean {
        val packageName = CustomTabsClient.getPackageName(context, null)
        return packageName != null
    }
}
