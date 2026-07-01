/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.recentcalls.impl.recentcalls.RecentCallsPresenter
import io.element.android.features.recentcalls.impl.recentcalls.RecentCallsState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@BindingContainer
@ContributesTo(SessionScope::class)
interface RecentCallsModule {
    @Binds
    fun bindRecentCallsPresenter(presenter: RecentCallsPresenter): Presenter<RecentCallsState>
}
