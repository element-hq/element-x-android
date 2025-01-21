/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl.troubleshoot

import android.Manifest
import android.os.Build
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.impl.R
import io.element.android.libraries.permissions.impl.action.PermissionActions
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class NotificationTroubleshootCheckPermissionTest @Inject constructor(
    private val permissionStateProvider: PermissionStateProvider,
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    private val permissionActions: PermissionActions,
    stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order: Int = 0

    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_check_permission_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_check_permission_description),
        hasQuickFix = true,
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )

    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            permissionStateProvider.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        delegate.done(result)
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        // Do not bother about asking the permission inline, just lead the user to the settings
        permissionActions.openSettings()
    }
}
