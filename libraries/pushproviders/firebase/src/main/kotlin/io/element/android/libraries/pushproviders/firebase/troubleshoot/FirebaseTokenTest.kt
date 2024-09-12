/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushproviders.firebase.FirebaseConfig
import io.element.android.libraries.pushproviders.firebase.FirebaseStore
import io.element.android.libraries.pushproviders.firebase.FirebaseTroubleshooter
import io.element.android.libraries.pushproviders.firebase.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class FirebaseTokenTest @Inject constructor(
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

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val token = firebaseStore.getFcmToken()
        if (token != null) {
            delegate.updateState(
                description = stringProvider.getString(
                    R.string.troubleshoot_notifications_test_firebase_token_success,
                    "${token.take(8)}*****"
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_token_failure),
                status = NotificationTroubleshootTestState.Status.Failure(true)
            )
        }
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        delegate.start()
        firebaseTroubleshooter.troubleshoot()
        run(coroutineScope)
    }
}
