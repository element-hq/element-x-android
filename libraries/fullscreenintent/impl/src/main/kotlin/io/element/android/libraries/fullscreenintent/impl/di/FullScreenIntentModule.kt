/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.impl.FullScreenIntentPermissionsPresenter

@ContributesTo(AppScope::class)
@Module
interface FullScreenIntentModule {
    @Binds
    fun bindFullScreenIntentPermissionsPresenter(presenter: FullScreenIntentPermissionsPresenter): Presenter<FullScreenIntentPermissionsState>
}
