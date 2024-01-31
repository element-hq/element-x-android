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

package io.element.android.x.info

import io.element.android.x.BuildConfig
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun logApplicationInfo() {
    val appVersion = buildString {
        append(BuildConfig.VERSION_NAME)
        append(" (")
        append(BuildConfig.VERSION_CODE)
        append(") - ")
        append(BuildConfig.BUILD_TYPE)
        append(" / ")
        append(BuildConfig.FLAVOR)
    }
    // TODO Get SDK version somehow
    val sdkVersion = "SDK VERSION (TODO)"
    val date = SimpleDateFormat("MM-dd HH:mm:ss.SSSZ", Locale.US).format(Date())

    Timber.d("----------------------------------------------------------------")
    Timber.d("----------------------------------------------------------------")
    Timber.d(" Application version: $appVersion")
    Timber.d(" Git SHA: ${BuildConfig.GIT_REVISION}")
    Timber.d(" SDK version: $sdkVersion")
    Timber.d(" Local time: $date")
    Timber.d("----------------------------------------------------------------")
    Timber.d("----------------------------------------------------------------\n\n\n\n")
}
