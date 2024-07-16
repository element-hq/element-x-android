/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.impl.pip

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

interface PipSupportProvider {
    @ChecksSdkIntAtLeast(Build.VERSION_CODES.O)
    fun isPipSupported(): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultPipSupportProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagService: FeatureFlagService,
) : PipSupportProvider {
    override fun isPipSupported(): Boolean {
        val isSupportedByTheOs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE).orFalse()
        return if (isSupportedByTheOs) {
            runBlocking { featureFlagService.isFeatureEnabled(FeatureFlags.PictureInPicture) }
        } else {
            false
        }
    }
}
