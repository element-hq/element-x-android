/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFeatureFlagServiceTest {
    private val aFeature = FakeFeature(
        key = "test_feature",
        title = "Test Feature",
    )

    @Test
    fun `given service without provider when feature is checked then it returns the default value`() = runTest {
        val featureWithDefaultToFalse = FakeFeature(
            key = "test_feature",
            title = "Test Feature",
            defaultValue = { false }
        )
        val featureWithDefaultToTrue = FakeFeature(
            key = "test_feature_2",
            title = "Test Feature 2",
            defaultValue = { true }
        )
        val buildMeta = aBuildMeta()
        val featureFlagService = createDefaultFeatureFlagService(buildMeta = buildMeta)
        featureFlagService.isFeatureEnabledFlow(featureWithDefaultToFalse).test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
        featureFlagService.isFeatureEnabledFlow(featureWithDefaultToTrue).test {
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given service without provider when set enabled feature is called then it returns false`() = runTest {
        val featureFlagService = createDefaultFeatureFlagService()
        val result = featureFlagService.setFeatureEnabled(aFeature, true)
        assertThat(result).isFalse()
    }

    @Test
    fun `given service with a runtime provider when set enabled feature is called then it returns true`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(featureFlagProvider),
            buildMeta = buildMeta,
        )
        val result = featureFlagService.setFeatureEnabled(aFeature, true)
        assertThat(result).isTrue()
    }

    @Test
    fun `given service with a runtime provider and feature enabled when feature is checked then it returns the correct value`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(featureFlagProvider),
            buildMeta = buildMeta
        )
        featureFlagService.setFeatureEnabled(aFeature, true)
        featureFlagService.isFeatureEnabledFlow(aFeature).test {
            assertThat(awaitItem()).isTrue()
            featureFlagService.setFeatureEnabled(aFeature, false)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `given service with 2 runtime providers when feature is checked then it uses the priority correctly`() = runTest {
        val buildMeta = aBuildMeta()
        val lowPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(LOW_PRIORITY, buildMeta)
        val highPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(HIGH_PRIORITY, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(lowPriorityFeatureFlagProvider, highPriorityFeatureFlagProvider),
            buildMeta = buildMeta
        )
        lowPriorityFeatureFlagProvider.setFeatureEnabled(aFeature, false)
        highPriorityFeatureFlagProvider.setFeatureEnabled(aFeature, true)
        featureFlagService.isFeatureEnabledFlow(aFeature).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `getAvailableFeatures should return expected features`() {
        val aFinishedLabFeature = FakeFeature(
            key = "finished_lab_feature",
            title = "Finished Lab Feature",
            isFinished = true,
            isInLabs = true,
        )
        val aFinishedDevFeature = FakeFeature(
            key = "finished_dev_feature",
            title = "Finished Dev Feature",
            isFinished = true,
            isInLabs = false,
        )
        val anUnfinishedLabFeature = FakeFeature(
            key = "unfinished_lab_feature",
            title = "Unfinished Lab Feature",
            isFinished = false,
            isInLabs = true,
        )
        val anUnfinishedDevFeature = FakeFeature(
            key = "unfinished_dev_feature",
            title = "Unfinished Dev Feature",
            isFinished = false,
            isInLabs = false,
        )
        val featureFlagService = createDefaultFeatureFlagService(
            features = listOf(
                aFinishedLabFeature,
                aFinishedDevFeature,
                anUnfinishedLabFeature,
                anUnfinishedDevFeature,
            ),
        )
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishedFeatures = false,
                isInLabs = true,
            )
        ).containsExactly(anUnfinishedLabFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishedFeatures = true,
                isInLabs = true,
            )
        ).containsExactly(aFinishedLabFeature, anUnfinishedLabFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishedFeatures = false,
                isInLabs = false,
            )
        ).containsExactly(anUnfinishedDevFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishedFeatures = true,
                isInLabs = false,
            )
        ).containsExactly(aFinishedDevFeature, anUnfinishedDevFeature)
    }
}

private fun createDefaultFeatureFlagService(
    providers: Set<FeatureFlagProvider> = emptySet(),
    buildMeta: BuildMeta = aBuildMeta(),
    features: List<Feature> = emptyList(),
) = DefaultFeatureFlagService(
    providers = providers,
    buildMeta = buildMeta,
    featuresProvider = { features }
)
