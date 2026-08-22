/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.feral.FeralPushConnectionMonitor
import io.element.android.libraries.pushproviders.feral.R
import io.element.android.libraries.pushproviders.feral.service.FeralPushServiceController
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * "Feral connection": is the connection service running and the socket of this session connected?
 * The fix restarts the service and asks to exempt Feral from battery optimisation (Doze).
 */
@ContributesIntoSet(SessionScope::class)
class FeralPushConnectionTest(
    private val sessionId: SessionId,
    private val monitor: FeralPushConnectionMonitor,
    private val serviceController: FeralPushServiceController,
    private val batteryOptimization: FeralPushBatteryOptimization,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 400
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.feral_push_troubleshoot_title),
        defaultDescription = stringProvider.getString(R.string.feral_push_troubleshoot_description),
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == FeralPushConfig.NAME
    }

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val status = monitor.status.value
        val ignoringBattery = batteryOptimization.isIgnoringBatteryOptimizations()
        when {
            !status.serviceRunning -> delegate.updateState(
                description = stringProvider.getString(R.string.feral_push_troubleshoot_failure_service),
                status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true),
            )
            !status.isConnected(sessionId) -> delegate.updateState(
                description = stringProvider.getString(R.string.feral_push_troubleshoot_failure_socket),
                status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true),
            )
            !ignoringBattery -> delegate.updateState(
                description = stringProvider.getString(R.string.feral_push_troubleshoot_success_battery),
                status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true, isCritical = false),
            )
            else -> delegate.updateState(
                description = stringProvider.getString(R.string.feral_push_troubleshoot_success),
                status = NotificationTroubleshootTestState.Status.Success,
            )
        }
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        if (!monitor.status.value.serviceRunning) {
            serviceController.startIfRegistered()
        }
        if (!batteryOptimization.isIgnoringBatteryOptimizations()) {
            batteryOptimization.requestIgnoringBatteryOptimizations()
        }
    }
}
