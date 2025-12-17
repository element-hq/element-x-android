/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
    const val PRODUCTION_APPLICATION_NAME: String = "Element"

    /**
     * Used in the strings to reference the Element Desktop client, for instance Element Web.
     * Cannot be empty.
     * For Element, the value is "Element". We use the same name for desktop and mobile for now.
     */
    const val DESKTOP_APPLICATION_NAME: String = "Element"
}
