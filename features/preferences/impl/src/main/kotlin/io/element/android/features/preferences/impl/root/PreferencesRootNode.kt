/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.logout.api.direct.DirectLogoutEvent
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.preferences.impl.account.PreferencesAccountCallback
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.emoji.api.picker.EmojiPickerRenderer

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PreferencesRootPresenter,
    private val emojiPickerRenderer: EmojiPickerRenderer,
    private val directLogoutView: DirectLogoutView,
) : Node(buildContext, plugins = plugins) {
    interface Callback : PreferencesAccountCallback, Plugin {
        fun navigateToAddAccount()
        fun navigateToAnalyticsSettings()
        fun navigateToAbout()
        fun navigateToDeveloperSettings()
        fun navigateToLockScreenSettings()
        fun navigateToMediaSettings()
        fun navigateToLocationSettings()
        fun navigateToLabs()
    }

    private val callback: Callback = callback()

    private fun onManageAccountClick(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()

        PreferencesRootView(
            state = state,
            emojiPickerRenderer = emojiPickerRenderer,
            modifier = modifier,
            onBackClick = this::navigateUp,
            onAddAccountClick = callback::navigateToAddAccount,
            onOpenAnalytics = callback::navigateToAnalyticsSettings,
            onOpenAbout = callback::navigateToAbout,
            onOpenDeveloperSettings = callback::navigateToDeveloperSettings,
            onOpenMediaSettings = callback::navigateToMediaSettings,
            onOpenLocationSettings = callback::navigateToLocationSettings,
            onOpenLabs = callback::navigateToLabs,
            onOpenLockScreenSettings = callback::navigateToLockScreenSettings,
            onOpenRageShake = callback::navigateToBugReport,
            onSecureBackupClick = callback::navigateToSecureBackup,
            onModerationAndSafetyClick = callback::navigateToModerationAndSafety,
            onLinkNewDeviceClick = callback::navigateToLinkNewDevice,
            onManageAccountClick = { onManageAccountClick(activity, it, isDark) },
            onOpenNotificationSettings = callback::navigateToNotificationSettings,
            onEditProfileClick = callback::navigateToUserProfile,
            onSignOutClick = {
                if (state.preferencesAccountState.directLogoutState.canDoDirectSignOut) {
                    state.preferencesAccountState.directLogoutState.eventSink(DirectLogoutEvent.Logout(ignoreSdkError = false))
                } else {
                    callback.startSignOutFlow()
                }
            },
            onDeactivateClick = callback::startAccountDeactivationFlow
        )

        directLogoutView.Render(state = state.preferencesAccountState.directLogoutState)
    }
}
