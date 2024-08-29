/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.logout.api.util

import android.app.Activity
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import timber.log.Timber

fun onSuccessLogout(
    activity: Activity,
    darkTheme: Boolean,
    url: String?,
) {
    Timber.d("Success logout with result url: $url")
    url?.let {
        activity.openUrlInChromeCustomTab(null, darkTheme, it)
    }
}
