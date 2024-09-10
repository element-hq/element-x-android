/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.impl.response.AcceptDeclineInvitePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@Module
interface InviteModule {
    @Binds
    fun bindAcceptDeclinePresenter(presenter: AcceptDeclineInvitePresenter): Presenter<AcceptDeclineInviteState>
}
