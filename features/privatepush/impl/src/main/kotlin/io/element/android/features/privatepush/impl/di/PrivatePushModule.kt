/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.privatepush.api.PrivatePushStatusState
import io.element.android.features.privatepush.impl.status.PrivatePushStatusPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface PrivatePushModule {
    @Binds
    fun bindPrivatePushStatusPresenter(presenter: PrivatePushStatusPresenter): Presenter<PrivatePushStatusState>
}
