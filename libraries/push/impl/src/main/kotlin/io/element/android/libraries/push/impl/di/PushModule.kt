/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.api.battery.BatteryOptimizationState
import io.element.android.libraries.push.impl.battery.BatteryOptimizationPresenter

@Module
@ContributesTo(AppScope::class)
interface PushModule {
    companion object {
        @Provides
        fun provideNotificationCompatManager(@ApplicationContext context: Context): NotificationManagerCompat {
            return NotificationManagerCompat.from(context)
        }
    }

    @Binds
    fun bindBatteryOptimizationPresenter(presenter: BatteryOptimizationPresenter): Presenter<BatteryOptimizationState>
}
