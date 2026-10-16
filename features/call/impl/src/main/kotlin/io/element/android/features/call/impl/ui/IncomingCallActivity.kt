/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.content.Intent
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
import io.element.android.features.call.api.CallData
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
import io.element.android.libraries.featureflag.api.FeatureFlagService
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

        /**
         * Answer straight away rather than showing the incoming-call screen.
         *
         * Set by the notification's Answer action, which has already asked the user - showing them
         * the same question again would be the bug. The screen is for the full-screen intent, where
         * the user has not answered anything yet.
         */
        const val EXTRA_ANSWER_IMMEDIATELY = "EXTRA_ANSWER_IMMEDIATELY"
    }

    @Inject
    lateinit var elementCallEntryPoint: ElementCallEntryPoint

    @Inject
    lateinit var activeCallManager: ActiveCallManager

    @Inject
    lateinit var appPreferencesStore: AppPreferencesStore

    @Inject
    lateinit var featureFlagService: FeatureFlagService

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
        if (answerImmediatelyIfAsked(intent)) return
        if (notificationData != null) {
            setContent {
                val colors by remember {
                    enterpriseService.semanticColorsFlow(sessionId = notificationData.sessionId)
                }.collectAsState(SemanticColorsLightDark.default)
                ElementThemeApp(
                    appPreferencesStore = appPreferencesStore,
                    featureFlagService = featureFlagService,
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

    /**
     * The Answer action can arrive while this activity is already showing the ringing screen, because
     * the full screen intent put it there and it is `singleTask`. That delivers a new intent instead
     * of recreating the activity, so the answer has to be handled here as well as in [onCreate] - and
     * [setIntent] has to run first, or everything reading `intent` afterwards would still see the one
     * the activity was created with.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        answerImmediatelyIfAsked(intent)
    }

    /**
     * Answers and finishes when the intent is the notification's Answer action, and reports whether
     * it did.
     *
     * Answering goes through this activity rather than straight to a call screen so that it goes
     * through `ElementCallEntryPoint`, which is what decides between the WebView and the native
     * stack. The notification action used to name `ElementCallActivity` outright, so answering from
     * the notification always opened the WebView however the flag was set.
     */
    private fun answerImmediatelyIfAsked(intent: Intent?): Boolean {
        if (intent == null || !intent.getBooleanExtra(EXTRA_ANSWER_IMMEDIATELY, false)) return false
        val notificationData = IntentCompat.getParcelableExtra(intent, EXTRA_NOTIFICATION_DATA, CallNotificationData::class.java)
            ?: return false
        onAnswer(notificationData)
        finish()
        return true
    }

    private fun onAnswer(notificationData: CallNotificationData) {
        elementCallEntryPoint.startCall(
            CallData(
                sessionId = notificationData.sessionId,
                roomId = notificationData.roomId,
                isAudioCall = notificationData.audioOnly,
            )
        )
    }

    private fun onCancel() {
        val activeCall = activeCallManager.activeCall.value ?: return
        appCoroutineScope.launch {
            activeCallManager.hangUpCall(callData = activeCall.callData)
        }
    }
}
