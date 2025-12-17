/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.api.sdk

import androidx.annotation.ChecksSdkIntAtLeast

interface BuildVersionSdkIntProvider {
    /**
     * Return the current version of the Android SDK.
     */
    fun get(): Int

    /**
     * Checks the if the current OS version is equal or greater than [version].
     * @return A `non-null` result if true, `null` otherwise.
     */
    @ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
    fun <T> whenAtLeast(version: Int, result: () -> T): T? {
        return if (get() >= version) {
            result()
        } else {
            null
        }
    }

    @ChecksSdkIntAtLeast(parameter = 0)
    fun isAtLeast(version: Int) = get() >= version
}
