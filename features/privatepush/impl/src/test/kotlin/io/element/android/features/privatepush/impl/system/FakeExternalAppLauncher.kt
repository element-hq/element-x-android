/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.system

class FakeExternalAppLauncher(
    var openAppResult: Boolean = true,
) : ExternalAppLauncher {
    val openedApps = mutableListOf<String>()
    val playStoreRequests = mutableListOf<String>()
    val fdroidRequests = mutableListOf<String>()

    override fun openApp(packageName: String): Boolean {
        openedApps += packageName
        return openAppResult
    }

    override fun openPlayStore(packageName: String) {
        playStoreRequests += packageName
    }

    override fun openFdroid(packageName: String) {
        fdroidRequests += packageName
    }
}
