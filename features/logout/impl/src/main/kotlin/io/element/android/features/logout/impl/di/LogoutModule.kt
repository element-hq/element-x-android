/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.impl.direct.DirectLogoutPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@Module
interface LogoutModule {
    @Binds
    fun bindDirectLogoutPresenter(presenter: DirectLogoutPresenter): Presenter<DirectLogoutState>
}
