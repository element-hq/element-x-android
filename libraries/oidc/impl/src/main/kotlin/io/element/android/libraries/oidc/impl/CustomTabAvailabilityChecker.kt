/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
