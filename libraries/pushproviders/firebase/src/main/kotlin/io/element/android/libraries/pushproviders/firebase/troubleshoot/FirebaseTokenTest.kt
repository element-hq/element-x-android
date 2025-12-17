/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase.troubleshoot

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.pushproviders.firebase.FirebaseConfig
import io.element.android.libraries.pushproviders.firebase.FirebaseStore
import io.element.android.libraries.pushproviders.firebase.FirebaseTroubleshooter
import io.element.android.libraries.pushproviders.firebase.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ContributesIntoSet(AppScope::class)
@Inject
class FirebaseTokenTest(
    private val firebaseStore: FirebaseStore,
    private val firebaseTroubleshooter: FirebaseTroubleshooter,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 310
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_token_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_token_description),
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.LONG_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == FirebaseConfig.NAME
    }

    private var currentJob: Job? = null
    override suspend fun run(coroutineScope: CoroutineScope) {
        currentJob?.cancel()
        delegate.start()
        currentJob = firebaseStore.fcmTokenFlow()
            .onEach { token ->
                if (token != null) {
                    delegate.updateState(
                        description = stringProvider.getString(
                            R.string.troubleshoot_notifications_test_firebase_token_success,
                            "*****${token.takeLast(8)}"
                        ),
                        status = NotificationTroubleshootTestState.Status.Success
                    )
                } else {
                    delegate.updateState(
                        description = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_token_failure),
                        status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true)
                    )
                }
            }
            .launchIn(coroutineScope)
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        delegate.start()
        firebaseTroubleshooter.troubleshoot()
        run(coroutineScope)
    }
}
