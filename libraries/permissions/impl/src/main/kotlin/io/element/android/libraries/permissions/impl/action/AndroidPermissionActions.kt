/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl.action

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.startNotificationSettingsIntent
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidPermissionActions @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionActions {
    override fun openSettings() {
        context.startNotificationSettingsIntent()
    }
}
