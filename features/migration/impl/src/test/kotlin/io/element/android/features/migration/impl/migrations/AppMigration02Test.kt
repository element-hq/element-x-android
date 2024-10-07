/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration02Test {
    @Test
    fun `test migration`() = runTest {
        val sessionStore = InMemorySessionStore().apply {
            updateData(aSessionData())
        }
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSessionVerificationSkipped = false)
        val sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(
            getLambda = lambdaRecorder { _, _, -> sessionPreferencesStore },
            removeLambda = lambdaRecorder { _ -> }
        )
        val migration = AppMigration02(sessionStore = sessionStore, sessionPreferenceStoreFactory = sessionPreferencesStoreFactory)

        migration.migrate()

        // We got the session preferences store
        sessionPreferencesStoreFactory.getLambda.assertions().isCalledOnce()
        // We changed the settings for the skipping the session verification
        assertThat(sessionPreferencesStore.isSessionVerificationSkipped().first()).isTrue()
        // We removed the session preferences store from cache
        sessionPreferencesStoreFactory.removeLambda.assertions().isCalledOnce()
    }
}
