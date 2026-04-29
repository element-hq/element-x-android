/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.store

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.impl.live.LiveLocationStore
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultLiveLocationStoreTest {
    private val preferenceDataStoreFactory = FakePreferenceDataStoreFactory()

    @Test
    fun `disclaimer defaults to false`() = runTest {
        val store = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )

        assertThat(store.hasAcceptedLiveLocationDisclaimer()).isFalse()
    }

    @Test
    fun `disclaimer acceptance is isolated per session`() = runTest {
        val firstStore = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = A_SESSION_ID,
        )
        val secondStore = LiveLocationStore(
            preferenceDataStoreFactory = preferenceDataStoreFactory,
            sessionId = SessionId("@other:server"),
        )

        firstStore.setAcceptedLiveLocationDisclaimer().getOrThrow()

        assertThat(firstStore.hasAcceptedLiveLocationDisclaimer()).isTrue()
        assertThat(secondStore.hasAcceptedLiveLocationDisclaimer()).isFalse()
    }
}
