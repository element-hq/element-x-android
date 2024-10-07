/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.login.impl.changeserver.ChangeServerPresenter
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
@Module
interface LoginModule {
    @Binds
    fun bindChangeServerPresenter(presenter: ChangeServerPresenter): Presenter<ChangeServerState>
}
