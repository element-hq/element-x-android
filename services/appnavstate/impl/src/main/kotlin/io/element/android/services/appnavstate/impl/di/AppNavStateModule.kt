/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl.di

import android.content.Context
import androidx.startup.AppInitializer
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.appnavstate.impl.initializer.AppForegroundStateServiceInitializer

@Module
@ContributesTo(AppScope::class)
object AppNavStateModule {
    @Provides
    fun provideAppForegroundStateService(
        @ApplicationContext context: Context
    ): AppForegroundStateService =
        AppInitializer.getInstance(context).initializeComponent(AppForegroundStateServiceInitializer::class.java)
}
