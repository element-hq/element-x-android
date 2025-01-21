/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFeatureFlagServiceTest {
    @Test
    fun `given service without provider when feature is checked then it returns the default value`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagService = DefaultFeatureFlagService(emptySet(), buildMeta)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.LocationSharing).test {
            assertThat(awaitItem()).isEqualTo(FeatureFlags.LocationSharing.defaultValue(buildMeta))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given service without provider when set enabled feature is called then it returns false`() = runTest {
        val featureFlagService = DefaultFeatureFlagService(emptySet(), aBuildMeta())
        val result = featureFlagService.setFeatureEnabled(FeatureFlags.LocationSharing, true)
        assertThat(result).isFalse()
    }

    @Test
    fun `given service with a runtime provider when set enabled feature is called then it returns true`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = DefaultFeatureFlagService(setOf(featureFlagProvider), buildMeta)
        val result = featureFlagService.setFeatureEnabled(FeatureFlags.LocationSharing, true)
        assertThat(result).isTrue()
    }

    @Test
    fun `given service with a runtime provider and feature enabled when feature is checked then it returns the correct value`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = DefaultFeatureFlagService(setOf(featureFlagProvider), buildMeta)
        featureFlagService.setFeatureEnabled(FeatureFlags.LocationSharing, true)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.LocationSharing).test {
            assertThat(awaitItem()).isTrue()
            featureFlagService.setFeatureEnabled(FeatureFlags.LocationSharing, false)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `given service with 2 runtime providers when feature is checked then it uses the priority correctly`() = runTest {
        val buildMeta = aBuildMeta()
        val lowPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(LOW_PRIORITY, buildMeta)
        val highPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(HIGH_PRIORITY, buildMeta)
        val featureFlagService = DefaultFeatureFlagService(setOf(lowPriorityFeatureFlagProvider, highPriorityFeatureFlagProvider), buildMeta)
        lowPriorityFeatureFlagProvider.setFeatureEnabled(FeatureFlags.LocationSharing, false)
        highPriorityFeatureFlagProvider.setFeatureEnabled(FeatureFlags.LocationSharing, true)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.LocationSharing).test {
            assertThat(awaitItem()).isTrue()
        }
    }
}
