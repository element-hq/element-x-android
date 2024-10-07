/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.analytics.api.preferences.AnalyticsPreferencesState
import io.element.android.features.analytics.impl.preferences.AnalyticsPreferencesPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
@Module
interface AnalyticsModule {
    @Binds
    fun bindAnalyticsPreferencesPresenter(presenter: AnalyticsPreferencesPresenter): Presenter<AnalyticsPreferencesState>
}
