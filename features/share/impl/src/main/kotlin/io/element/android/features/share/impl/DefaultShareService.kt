/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.share.api.ShareService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultShareService @Inject constructor(
    private val featureFlagService: FeatureFlagService,
    @ApplicationContext private val context: Context,
) : ShareService {
    override fun observeFeatureFlag(coroutineScope: CoroutineScope) {
        val shareActivityComponent = getShareActivityComponent()
            ?: return Unit.also {
                Timber.w("ShareActivity not found")
            }
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.IncomingShare)
            .onEach { enabled ->
                shareActivityComponent.enableOrDisable(enabled)
            }
            .launchIn(coroutineScope)
    }

    private fun getShareActivityComponent(): ComponentName? {
        return context.packageManager
            .getPackageInfo(
                context.packageName,
                PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
            )
            .activities
            ?.firstOrNull { it.name.endsWith(".ShareActivity") }
            ?.let { shareActivityInfo ->
                ComponentName(
                    shareActivityInfo.packageName,
                    shareActivityInfo.name,
                )
            }
    }

    private fun ComponentName.enableOrDisable(enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        try {
            context.packageManager.setComponentEnabledSetting(
                this,
                state,
                PackageManager.DONT_KILL_APP,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable or disable the component")
        }
    }
}
