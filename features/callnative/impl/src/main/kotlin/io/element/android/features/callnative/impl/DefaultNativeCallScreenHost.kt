/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.call.api.ElementCallController
import io.element.android.call.impl.ElementCallPictureInPicture
import io.element.android.call.ui.theme.ElementCallStyle
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.features.callnative.api.NativeCallScreenHost
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.api.handleSecureFlag

/**
 * App-scoped, like the call screen it serves: that screen can open over the lock screen with no
 * session on screen, so it takes whichever session currently has a call.
 */
@ContributesBinding(AppScope::class)
class DefaultNativeCallScreenHost(
    private val controllers: ElementCallControllers,
    private val lockScreenService: LockScreenService,
) : NativeCallScreenHost {
    override val elementCallController: ElementCallController?
        get() = controllers.withRunningCall()

    /**
     * The launcher Activity, which is `MainActivity`: `singleTask`, so an instance already under the call
     * screen is brought back rather than a second one started, and its own lock screen applies there.
     */
    override fun leaveCallScreenIntent(context: Context): Intent =
        checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    override fun onCallActivityCreated(activity: ComponentActivity, controller: ElementCallController) {
        ElementCallPictureInPicture.attach(activity, controller)
        // As MainActivity does: with a PIN set, the call is kept out of screenshots of Recents too.
        lockScreenService.handleSecureFlag(activity)
    }

    @Composable
    override fun CallScreenContent(content: @Composable (style: ElementCallStyle?) -> Unit) {
        // The call is always dark, and the style's avatars are drawn with Compound's colours.
        ElementTheme(theme = Theme.Dark, applySystemBarsUpdate = false) {
            content(rememberElementXCallStyle())
        }
    }
}
