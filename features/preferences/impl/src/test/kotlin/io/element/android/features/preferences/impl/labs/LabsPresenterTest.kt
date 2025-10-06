/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.labs

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LabsPresenterTest {
    @Test
    fun `present - ensures only unfinished features in labs are displayed`() = runTest {
        val availableFeatures = listOf(
            FakeFeature(
                key = "feature_1",
                title = "Feature 1",
                isInLabs = true,
            ),
            FakeFeature(
                key = "feature_2",
                title = "Feature 2",
                isInLabs = false,
            ),
            FakeFeature(
                key = "feature_3",
                title = "Feature 3",
                isInLabs = true,
                isFinished = true,
            )
        )
        createLabsPresenter(
            availableFeatures = availableFeatures,
        ).test {
            val receivedFeatures = awaitItem().features
            assertThat(receivedFeatures).hasSize(1)
            assertThat(receivedFeatures.first().key).isEqualTo(availableFeatures.first().key)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - ToggleFeature actually toggles the value`() = runTest {
        val availableFeatures = listOf(
            FakeFeature(
                key = "feature_1",
                title = "Feature 1",
                isInLabs = true,
            ),
        )
        createLabsPresenter(
            availableFeatures = availableFeatures,
        ).test {
            val initialItem = awaitItem()
            val feature = initialItem.features.first()
            assertThat(feature.isEnabled).isFalse()

            // Wait until the data finished loading
            skipItems(1)

            // Toggle the feature, should be true now
            initialItem.eventSink(LabsEvents.ToggleFeature(feature.key))
            assertThat(awaitItem().features.first().isEnabled).isTrue()

            // Toggle the feature, should be false now
            initialItem.eventSink(LabsEvents.ToggleFeature(feature.key))
            assertThat(awaitItem().features.first().isEnabled).isFalse()
        }
    }

    private fun createLabsPresenter(
        availableFeatures: List<Feature> = emptyList(),
    ): LabsPresenter {
        return LabsPresenter(
            stringProvider = FakeStringProvider(),
            featureFlagService = FakeFeatureFlagService(providedAvailableFeatures = availableFeatures),
        )
    }
}
