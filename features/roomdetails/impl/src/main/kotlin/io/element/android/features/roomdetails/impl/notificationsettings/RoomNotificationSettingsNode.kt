/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.notificationsettings

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.push.api.notifications.RoomNotificationSettingsIntentProvider
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomNotificationSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomNotificationSettingsPresenter.Factory,
    @ApplicationContext private val context: Context,
    private val room: JoinedRoom,
    private val roomNotificationSettingsIntentProvider: RoomNotificationSettingsIntentProvider,
    private val analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    data class RoomNotificationSettingInput(
        val showUserDefinedSettingStyle: Boolean
    ) : NodeInputs

    interface Callback : Plugin {
        fun navigateToGlobalNotificationSettings()
    }

    private val callback: Callback = callback()
    private val inputs = inputs<RoomNotificationSettingInput>()

    private val presenter = presenterFactory.create(inputs.showUserDefinedSettingStyle)

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomNotifications))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RoomNotificationSettingsView(
            state = state,
            modifier = modifier,
            onShowGlobalNotifications = callback::navigateToGlobalNotificationSettings,
            onShowAndroidRoomNotificationSettings = ::openAndroidRoomNotificationSettings,
            onBackClick = ::navigateUp,
        )
    }

    private fun openAndroidRoomNotificationSettings() {
        lifecycleScope.launch {
            val roomInfo = room.info()
            val roomDisplayName = roomInfo.name?.takeIf { it.isNotBlank() } ?: room.roomId.value
            val intent = roomNotificationSettingsIntentProvider.getIntent(
                sessionId = room.sessionId,
                roomId = room.roomId,
                roomDisplayName = roomDisplayName,
                isDm = room.isDm(),
                roomAvatarUrl = roomInfo.avatarUrl,
            )
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                context.toast(io.element.android.libraries.androidutils.R.string.error_no_compatible_app_found)
            }
        }
    }
}
