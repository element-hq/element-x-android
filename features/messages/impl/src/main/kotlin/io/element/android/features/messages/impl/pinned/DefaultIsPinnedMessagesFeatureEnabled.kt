/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.messages.api.pinned.IsPinnedMessagesFeatureEnabled
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultIsPinnedMessagesFeatureEnabled @Inject constructor(
    private val featureFlagService: FeatureFlagService,
) : IsPinnedMessagesFeatureEnabled {
    @Composable
    override operator fun invoke(): Boolean {
        var isFeatureEnabled by rememberSaveable {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit) {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.PinnedEvents)
                .onEach { isFeatureEnabled = it }
                .launchIn(this)
        }
        return isFeatureEnabled
    }
}
