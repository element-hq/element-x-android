/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.home.impl.spaces.HomeSpacesPresenter
import io.element.android.features.home.impl.spaces.HomeSpacesState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@BindingContainer
@ContributesTo(SessionScope::class)
interface HomeSpacesModule {
    @Binds
    fun bindHomeSpacesPresenter(presenter: HomeSpacesPresenter): Presenter<HomeSpacesState>
}
