/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.system

class FakeInstalledAppsDetector(
    val installed: MutableMap<String, Long> = mutableMapOf(),
) : InstalledAppsDetector {
    override fun isInstalled(packageName: String): Boolean = installed.containsKey(packageName)
    override fun installedVersionCode(packageName: String): Long? = installed[packageName]
}
