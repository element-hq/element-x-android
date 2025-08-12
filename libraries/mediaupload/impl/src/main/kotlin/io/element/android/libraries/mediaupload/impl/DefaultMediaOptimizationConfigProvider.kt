/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultMediaOptimizationConfigProvider @Inject constructor(
    private val sessionPreferencesStore: SessionPreferencesStore,
) : MediaOptimizationConfigProvider {
    override suspend fun get(): MediaOptimizationConfig = MediaOptimizationConfig(
        compressImages = sessionPreferencesStore.doesOptimizeImages().first(),
        videoCompressionPreset = sessionPreferencesStore.getVideoCompressionPreset().first(),
    )
}
