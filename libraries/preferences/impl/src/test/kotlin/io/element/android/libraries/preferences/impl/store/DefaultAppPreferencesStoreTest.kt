/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAppPreferencesStoreTest {
    private val buildMeta = BuildMeta(
        buildType = BuildType.DEBUG,
        isDebuggable = true,
        applicationName = "Element X",
        productionApplicationName = "Element",
        desktopApplicationName = "Element Desktop",
        applicationId = "io.element.android",
        isEnterpriseBuild = false,
        lowPrivacyLoggingEnabled = false,
        versionName = "1.0.0",
        versionCode = 1,
        gitRevision = "test",
        gitBranchName = "test",
        flavorDescription = "test",
        flavorShortDescription = "test",
    )

    @Test
    fun `live location minimum distance defaults to 10`() = runTest {
        val store = DefaultAppPreferencesStore(
            buildMeta = buildMeta,
            preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
        )

        assertThat(store.getLiveLocationMinimumDistanceUpdateFlow().first()).isEqualTo(10)
    }

    @Test
    fun `live location minimum distance persists updates`() = runTest {
        val store = DefaultAppPreferencesStore(
            buildMeta = buildMeta,
            preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
        )

        store.setLiveLocationMinimumDistanceUpdate(25)

        assertThat(store.getLiveLocationMinimumDistanceUpdateFlow().first()).isEqualTo(25)
    }
}
