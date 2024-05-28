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

package io.element.android.appconfig

object ApplicationConfig {
    /**
     * Application name used in the UI for string. If empty, the value is taken from the resources `R.string.app_name`.
     * Note that this value is not used for the launcher icon.
     * For Element, the value is empty, and so read from `R.string.app_name`, which depends on the build variant:
     * - "Element X" for release builds;
     * - "Element X dbg" for debug builds;
     * - "Element X nightly" for nightly builds.
     */
    const val APPLICATION_NAME: String = ""

    /**
     * Used in the strings to reference the Element client.
     * Cannot be empty.
     * For Element, the value is "Element".
     */
    const val PRODUCTION_APPLICATION_NAME: String = "Parolla"

    /**
     * Used in the strings to reference the Element Desktop client, for instance Element Web.
     * Cannot be empty.
     * For Element, the value is "Element". We use the same name for desktop and mobile for now.
     */
    const val DESKTOP_APPLICATION_NAME: String = "Parolla"

    /**
     * The maximum size of the upload request. Default value is just below CloudFlare's max request size.
     */
    const val MAX_LOG_UPLOAD_SIZE = 50 * 1024 * 1024L
}
