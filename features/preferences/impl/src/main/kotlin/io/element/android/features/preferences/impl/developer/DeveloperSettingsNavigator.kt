/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

/**
 * Navigation performed by the developer settings screen, when data has to be loaded first.
 */
fun interface DeveloperSettingsNavigator {
    fun openPushRules(filename: String, content: String)
}
