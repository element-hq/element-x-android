/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.preferences.impl.account.PreferencesAccountPresenter
import io.element.android.features.preferences.impl.account.PreferencesAccountState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface PreferencesBindingContainer {
    @Binds
    fun bindPreferencesAccountPresenter(presenter: PreferencesAccountPresenter): Presenter<PreferencesAccountState>
}
