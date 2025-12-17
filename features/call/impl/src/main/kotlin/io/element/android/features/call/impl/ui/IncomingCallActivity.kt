/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Inject
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.features.call.impl.utils.CallState
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Activity that's displayed as a full screen intent when an incoming call is received.
 */
class IncomingCallActivity : AppCompatActivity() {
    companion object {
        /**
         * Extra key for the notification data.
         */
        const val EXTRA_NOTIFICATION_DATA = "EXTRA_NOTIFICATION_DATA"
    }

    @Inject
    lateinit var elementCallEntryPoint: ElementCallEntryPoint

    @Inject
    lateinit var activeCallManager: ActiveCallManager

    @Inject
    lateinit var appPreferencesStore: AppPreferencesStore

    @Inject
    lateinit var enterpriseService: EnterpriseService

    @Inject
    lateinit var buildMeta: BuildMeta

    @AppCoroutineScope
    @Inject lateinit var appCoroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings<CallBindings>().inject(this)

        // Set flags so it can be displayed in the lock screen
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val notificationData = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_NOTIFICATION_DATA, CallNotificationData::class.java) }
        if (notificationData != null) {
            setContent {
                val colors by remember {
                    enterpriseService.semanticColorsFlow(sessionId = notificationData.sessionId)
                }.collectAsState(SemanticColorsLightDark.default)
                ElementThemeApp(
                    appPreferencesStore = appPreferencesStore,
                    compoundLight = colors.light,
                    compoundDark = colors.dark,
                    buildMeta = buildMeta,
                ) {
                    IncomingCallScreen(
                        notificationData = notificationData,
                        onAnswer = ::onAnswer,
                        onCancel = ::onCancel,
                    )
                }
            }
        } else {
            // No data, finish the activity
            finish()
            return
        }

        activeCallManager.activeCall
            .filter { it?.callState !is CallState.Ringing }
            .onEach { finish() }
            .launchIn(lifecycleScope)
    }

    private fun onAnswer(notificationData: CallNotificationData) {
        elementCallEntryPoint.startCall(CallType.RoomCall(notificationData.sessionId, notificationData.roomId))
    }

    private fun onCancel() {
        val activeCall = activeCallManager.activeCall.value ?: return
        appCoroutineScope.launch {
            activeCallManager.hungUpCall(callType = activeCall.callType)
        }
    }
}
