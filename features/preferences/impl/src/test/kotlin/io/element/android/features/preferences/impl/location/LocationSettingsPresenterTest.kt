/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.location

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LocationSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createLocationSettingsPresenter().test {
            with(awaitItem()) {
                assertThat(liveLocationMinimumDistanceUpdate).isEqualTo(10)
            }
        }
    }

    @Test
    fun `present - exposes live location minimum distance from app preferences`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore(
            liveLocationMinimumDistanceUpdate = 50,
        )
        createLocationSettingsPresenter(appPreferencesStore = appPreferencesStore).test {
            skipItems(1)

            with(awaitItem()) {
                assertThat(liveLocationMinimumDistanceUpdate).isEqualTo(50)
            }
        }
    }

    @Test
    fun `present - saving live location minimum distance updates app preferences`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore(
            liveLocationMinimumDistanceUpdate = 10,
        )
        createLocationSettingsPresenter(appPreferencesStore = appPreferencesStore).test {
            with(awaitItem()) {
                assertThat(liveLocationMinimumDistanceUpdate).isEqualTo(10)
                eventSink(LocationSettingsEvent.SetLiveLocationMinimumDistanceUpdate(42))
            }
            with(awaitItem()) {
                assertThat(liveLocationMinimumDistanceUpdate).isEqualTo(42)
            }
        }
        assertThat(appPreferencesStore.getLiveLocationMinimumDistanceInMetersUpdateFlow().first()).isEqualTo(42)
    }

    private fun CoroutineScope.createLocationSettingsPresenter(
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ) = LocationSettingsPresenter(
        appPreferencesStore = appPreferencesStore,
        sessionCoroutineScope = this,
    )
}
