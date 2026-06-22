/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.preferences.impl.developer.appsettings.AppDeveloperSettingsPresenter
import io.element.android.features.preferences.impl.developer.appsettings.AppDeveloperSettingsState
import io.element.android.libraries.architecture.Presenter

@ContributesTo(AppScope::class)
@BindingContainer
interface DeveloperSettingsModule {
    @Binds
    fun bindAppDeveloperSettingsPresenter(presenter: AppDeveloperSettingsPresenter): Presenter<AppDeveloperSettingsState>
}
