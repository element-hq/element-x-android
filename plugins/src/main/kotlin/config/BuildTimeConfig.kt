/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    const val APPLICATION_ID = "io.element.android.x"
    const val APPLICATION_NAME = "Element X"
    const val GOOGLE_APP_ID_RELEASE = "1:912726360885:android:d097de99a4c23d2700427c"
    const val GOOGLE_APP_ID_DEBUG = "1:912726360885:android:def0a4e454042e9b00427c"
    const val GOOGLE_APP_ID_NIGHTLY = "1:912726360885:android:e17435e0beb0303000427c"

    val SERVICES_MAPTILER_APIKEY: String? = null
    val SERVICES_MAPTILER_LIGHT_MAPID: String? = null
    val SERVICES_MAPTILER_DARK_MAPID: String? = null
}
