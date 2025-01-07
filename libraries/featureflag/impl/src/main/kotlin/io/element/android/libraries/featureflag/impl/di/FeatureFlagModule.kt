/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.featureflag.impl.FeatureFlagProvider
import io.element.android.libraries.featureflag.impl.PreferencesFeatureFlagProvider

@Module
@ContributesTo(AppScope::class)
object FeatureFlagModule {
    @JvmStatic
    @Provides
    @ElementsIntoSet
    fun providesFeatureFlagProvider(
        mutableFeatureFlagProvider: PreferencesFeatureFlagProvider,
    ): Set<FeatureFlagProvider> {
        return buildSet {
            add(mutableFeatureFlagProvider)
        }
    }
}
