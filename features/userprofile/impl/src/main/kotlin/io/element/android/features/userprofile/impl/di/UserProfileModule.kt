/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId

@Module
@ContributesTo(SessionScope::class)
object UserProfileModule {
    @Provides
    fun provideUserProfilePresenterFactory(
        matrixClient: MatrixClient,
        startDMAction: StartDMAction,
        dispatchers: CoroutineDispatchers,
        clipboardHelper: ClipboardHelper,
        snackbarDispatcher: SnackbarDispatcher,
    ): UserProfilePresenter.Factory {
        return object : UserProfilePresenter.Factory {
            override fun create(userId: UserId): UserProfilePresenter {
                return UserProfilePresenter(userId, matrixClient, startDMAction, dispatchers, clipboardHelper, snackbarDispatcher)
            }
        }
    }
}
