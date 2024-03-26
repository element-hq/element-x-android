/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.permissions.impl.troubleshoot

import android.Manifest
import android.os.Build
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestDelegate
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.impl.action.PermissionActions
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class NotificationTroubleshootCheckPermissionTest @Inject constructor(
    private val permissionStateProvider: PermissionStateProvider,
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    private val permissionActions: PermissionActions,
) : NotificationTroubleshootTest {
    override val order: Int = 0

    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = "Check permissions",
        defaultDescription = "Ensure that the application can show notifications.",
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

    override fun reset() = delegate.reset()

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        // Do not bother about asking the permission inline, just lead the user to the settings
        permissionActions.openSettings()
    }
}
