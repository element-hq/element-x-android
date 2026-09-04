/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

// Every assertion is made against a provider whose build type gives the opposite initial value, so a
// passing test proves the stored preference won rather than the default.
class ShowDeveloperSettingsProviderTest {
    @Test
    fun `tapping fewer times than required leaves the settings hidden`() = runTest {
        val store = InMemoryAppPreferencesStore(showDeveloperSettings = false)
        val provider = createProvider(store, BuildType.DEBUG)

        repeat(ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER - 1) {
            provider.unlockDeveloperSettings(backgroundScope)
        }
        runCurrent()

        assertThat(provider.showDeveloperSettings.value).isFalse()
    }

    @Test
    fun `unlocking is still in effect for a provider created later`() = runTest {
        val store = InMemoryAppPreferencesStore(showDeveloperSettings = false)
        val provider = createProvider(store, BuildType.RELEASE)

        repeat(ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER) {
            provider.unlockDeveloperSettings(backgroundScope)
        }
        runCurrent()
        val later = createProvider(store, BuildType.RELEASE)
        runCurrent()

        assertThat(later.showDeveloperSettings.value).isTrue()
    }

    @Test
    fun `switching off is still in effect for a provider created later`() = runTest {
        val store = InMemoryAppPreferencesStore(showDeveloperSettings = true)
        val provider = createProvider(store, BuildType.DEBUG)

        provider.setShowDeveloperSettings(false)
        runCurrent()
        val later = createProvider(store, BuildType.DEBUG)
        runCurrent()

        assertThat(later.showDeveloperSettings.value).isFalse()
    }

    @Test
    fun `after switching off, a single tap does not bring the settings back`() = runTest {
        val store = InMemoryAppPreferencesStore(showDeveloperSettings = false)
        val provider = createProvider(store, BuildType.DEBUG)
        repeat(ShowDeveloperSettingsProvider.DEVELOPER_SETTINGS_COUNTER) {
            provider.unlockDeveloperSettings(backgroundScope)
        }
        runCurrent()
        provider.setShowDeveloperSettings(false)
        runCurrent()

        provider.unlockDeveloperSettings(backgroundScope)
        runCurrent()

        assertThat(provider.showDeveloperSettings.value).isFalse()
    }

    private fun TestScope.createProvider(
        store: InMemoryAppPreferencesStore,
        buildType: BuildType,
    ) = ShowDeveloperSettingsProvider(
        buildMeta = aBuildMeta(buildType),
        appPreferencesStore = store,
        appCoroutineScope = backgroundScope,
    )
}
