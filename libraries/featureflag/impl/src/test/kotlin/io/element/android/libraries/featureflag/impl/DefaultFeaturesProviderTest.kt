/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import org.junit.Test

class DefaultFeaturesProviderTest {
    @Test
    fun `provide should return all features`() {
        val provider = DefaultFeaturesProvider()
        val features = provider.provide()
        assertThat(features.size).isEqualTo(FeatureFlags.entries.size)
        FeatureFlags.entries.forEach {
            assertThat(features.contains(it)).isTrue()
        }
    }
}
