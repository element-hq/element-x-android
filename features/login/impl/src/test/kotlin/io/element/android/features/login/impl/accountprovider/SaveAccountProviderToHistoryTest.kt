/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SaveAccountProviderToHistoryTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `invoke saves the current account provider url to history`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val accountProviderDataSource = anAccountProviderDataSource()
        accountProviderDataSource.setUrl("https://example.com")
        val sut = SaveAccountProviderToHistory(accountProviderDataSource, appPreferencesStore)

        sut()

        assertThat(appPreferencesStore.getHomeserverHistoryFlow().first())
            .containsExactly("https://example.com")
    }
}
